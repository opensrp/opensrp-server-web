package org.opensrp.web.acl;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opensrp.domain.AssignedLocations;
import org.smartregister.domain.Jurisdiction;
import org.smartregister.domain.PlanDefinition;
import org.opensrp.domain.Practitioner;
import org.opensrp.repository.LocationRepository;
import org.opensrp.repository.PractitionerRepository;
import org.opensrp.repository.PractitionerRoleRepository;
import org.opensrp.service.OrganizationService;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PractitionerRoleService;
import org.opensrp.service.PractitionerService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PlanPermissionEvaluatorTest {

	@Mock
	private Authentication authentication;

	@InjectMocks
	private PlanPermissionEvaluator planPermissionEvaluator;

	@Mock
	private PhysicalLocationService locationService;

	@Mock
	private LocationRepository locationRepository;

	@Mock
	private PractitionerService practitionerService;

	@Mock
	private OrganizationService organizationService;

	@Mock
	private PractitionerRoleService practitionerRoleService;

	@Mock
	private PractitionerRoleRepository practitionerRoleRepository;

	@Mock
	private PractitionerRepository practitionerRepository;

	private List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");

	@Before
	public void setup() {
		initMocks(this);
		planPermissionEvaluator = new PlanPermissionEvaluator();
		locationService.setLocationRepository(locationRepository);
		planPermissionEvaluator.setLocationService(locationService);
		when(authentication.getName()).thenReturn("user");
		when(authentication.getAuthorities()).thenAnswer(a -> roles.stream().map(role -> new GrantedAuthority() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getAuthority() {
				return role;
			}
		}).collect(Collectors.toList()));
	}

	@Test
	public void testHasPermission() {
		PlanDefinition planDefinition = new PlanDefinition();
		planDefinition.setIdentifier("cd09a3d4-01d9-485c-a1c5-a2eb078a61be");

		List<AssignedLocations> assignedLocations = new ArrayList<>();
		AssignedLocations assignedLocation = new AssignedLocations();
		assignedLocation.setPlanId("cd09a3d4-01d9-485c-a1c5-a2eb078a61be");
		assignedLocation.setJurisdictionId("cd09a3d4-01d9-485c-a1c5-a2eb078a61be");
		assignedLocations.add(assignedLocation);
		List<Long> organizationalIds = Collections.singletonList(12233l);
		Practitioner practitioner = new Practitioner();
		practitioner.setIdentifier("practitioner-id");
		ImmutablePair<Practitioner, List<Long>> practitionerListImmutablePair = new ImmutablePair<>(practitioner,
				organizationalIds);

		List<org.opensrp.domain.postgres.PractitionerRole> practitionerRoles = new ArrayList<>();
		org.opensrp.domain.postgres.PractitionerRole practitionerRole = new org.opensrp.domain.postgres.PractitionerRole();
		practitionerRole.setIdentifier("practitioner-role-id");
		practitionerRole.setOrganizationId(12345l);
		practitionerRoles.add(practitionerRole);

		doReturn(practitionerRoles).when(practitionerRoleService).getPgRolesForPractitioner(anyString());
		doReturn(practitionerRoles).when(practitionerRoleRepository).getPgRolesForPractitioner(anyString());
		doReturn(practitioner).when(practitionerRepository).getPractitionerByUserId(anyString());
		doReturn(practitionerListImmutablePair).when(practitionerService).getOrganizationsByUserId(anyString());
		doReturn(assignedLocations).when(organizationService).findAssignedLocationsAndPlans(any(List.class));
		doReturn(assignedLocations).when(locationService).getAssignedLocations(anyString());
		boolean hasPermission = planPermissionEvaluator.hasPermission(authentication,planDefinition);
		assertTrue(hasPermission);
	}

	@Test
	public void testHasPermissionV2() {
		PlanDefinition planDefinition = new PlanDefinition();
		planDefinition.setIdentifier("");
		List<Jurisdiction> jurisdictions = new ArrayList<>();
		Jurisdiction jurisdiction = new Jurisdiction();
		jurisdiction.setCode("cd09a3d4-01d9-485c-a1c5-a2eb078a61be");
		jurisdictions.add(jurisdiction);
		planDefinition.setJurisdiction(jurisdictions);

		List<AssignedLocations> assignedLocations = new ArrayList<>();
		AssignedLocations assignedLocation = new AssignedLocations();
		assignedLocation.setPlanId("cd09a3d4-01d9-485c-a1c5-a2eb078a61be");
		assignedLocation.setJurisdictionId("cd09a3d4-01d9-485c-a1c5-a2eb078a61be");
		assignedLocations.add(assignedLocation);
		List<Long> organizationalIds = Collections.singletonList(12233l);
		Practitioner practitioner = new Practitioner();
		practitioner.setIdentifier("practitioner-id");
		ImmutablePair<Practitioner, List<Long>> practitionerListImmutablePair = new ImmutablePair<>(practitioner,
				organizationalIds);

		List<org.opensrp.domain.postgres.PractitionerRole> practitionerRoles = new ArrayList<>();
		org.opensrp.domain.postgres.PractitionerRole practitionerRole = new org.opensrp.domain.postgres.PractitionerRole();
		practitionerRole.setIdentifier("practitioner-role-id");
		practitionerRole.setOrganizationId(12345l);
		practitionerRoles.add(practitionerRole);

		doReturn(practitionerRoles).when(practitionerRoleService).getPgRolesForPractitioner(anyString());
		doReturn(practitionerRoles).when(practitionerRoleRepository).getPgRolesForPractitioner(anyString());
		doReturn(practitioner).when(practitionerRepository).getPractitionerByUserId(anyString());
		doReturn(practitionerListImmutablePair).when(practitionerService).getOrganizationsByUserId(anyString());
		doReturn(assignedLocations).when(organizationService).findAssignedLocationsAndPlans(any(List.class));
		doReturn(assignedLocations).when(locationService).getAssignedLocations(anyString());
		boolean hasPermission = planPermissionEvaluator.hasPermission(authentication,planDefinition);
		assertTrue(hasPermission);

	}

	@Test
	public void testHasObjectPermissionWithStringAsTargetObject() {
		List<AssignedLocations> assignedLocations = new ArrayList<>();
		AssignedLocations assignedLocation = new AssignedLocations();
		assignedLocation.setPlanId("cd09a3d4-01d9-485c-a1c5-a2eb078a61be");

		assignedLocations.add(assignedLocation);
		List<Long> organizationalIds = Collections.singletonList(12233l);
		Practitioner practitioner = new Practitioner();
		practitioner.setIdentifier("practitioner-id");
		ImmutablePair<Practitioner, List<Long>> practitionerListImmutablePair = new ImmutablePair<>(practitioner,
				organizationalIds);

		List<org.opensrp.domain.postgres.PractitionerRole> practitionerRoles = new ArrayList<>();
		org.opensrp.domain.postgres.PractitionerRole practitionerRole = new org.opensrp.domain.postgres.PractitionerRole();
		practitionerRole.setIdentifier("practitioner-role-id");
		practitionerRole.setOrganizationId(12345l);
		practitionerRoles.add(practitionerRole);

		doReturn(practitionerRoles).when(practitionerRoleService).getPgRolesForPractitioner(anyString());
		doReturn(practitionerRoles).when(practitionerRoleRepository).getPgRolesForPractitioner(anyString());
		doReturn(practitioner).when(practitionerRepository).getPractitionerByUserId(anyString());
		doReturn(practitionerListImmutablePair).when(practitionerService).getOrganizationsByUserId(anyString());
		doReturn(assignedLocations).when(organizationService).findAssignedLocationsAndPlans(any(List.class));
		doReturn(assignedLocations).when(locationService).getAssignedLocations(anyString());
		boolean hasPermission = planPermissionEvaluator.hasObjectPermission(authentication, "cd09a3d4-01d9-485c-a1c5-a2eb078a61be",null);
		assertTrue(hasPermission);
	}

	@Test
	public void testHasObjectPermissionWithCollectionOfStringAsTargetObject() {
		List<String> collectionOfString = new ArrayList<>();
		collectionOfString.add("cd09a3d4-01d9-485c-a1c5-a2eb078a61bf");
		collectionOfString.add("cd09a3d4-01d9-485c-a1c5-a2eb078a61be");

		List<AssignedLocations> assignedLocations = new ArrayList<>();
		AssignedLocations assignedLocation = new AssignedLocations();
		assignedLocation.setPlanId("cd09a3d4-01d9-485c-a1c5-a2eb078a61bf");
		assignedLocations.add(assignedLocation);
		List<Long> organizationalIds = Collections.singletonList(12233l);
		Practitioner practitioner = new Practitioner();
		practitioner.setIdentifier("practitioner-id");
		ImmutablePair<Practitioner, List<Long>> practitionerListImmutablePair = new ImmutablePair<>(practitioner,
				organizationalIds);

		List<org.opensrp.domain.postgres.PractitionerRole> practitionerRoles = new ArrayList<>();
		org.opensrp.domain.postgres.PractitionerRole practitionerRole = new org.opensrp.domain.postgres.PractitionerRole();
		practitionerRole.setIdentifier("practitioner-role-id");
		practitionerRole.setOrganizationId(12345l);
		practitionerRoles.add(practitionerRole);

		doReturn(practitionerRoles).when(practitionerRoleService).getPgRolesForPractitioner(anyString());
		doReturn(practitionerRoles).when(practitionerRoleRepository).getPgRolesForPractitioner(anyString());
		doReturn(practitioner).when(practitionerRepository).getPractitionerByUserId(anyString());
		doReturn(practitionerListImmutablePair).when(practitionerService).getOrganizationsByUserId(anyString());
		doReturn(assignedLocations).when(organizationService).findAssignedLocationsAndPlans(any(List.class));
		doReturn(assignedLocations).when(locationService).getAssignedLocations(anyString());
		boolean hasPermission = planPermissionEvaluator.hasObjectPermission(authentication, (Serializable) collectionOfString,null);
		assertTrue(hasPermission);
	}

	@Test
	public void testHasObjectPermissionWithPlanAsTargetObject() {
		PlanDefinition planDefinition = new PlanDefinition();
		planDefinition.setIdentifier("cd09a3d4-01d9-485c-a1c5-a2eb078a61bf");
		List<AssignedLocations> assignedLocations = new ArrayList<>();
		AssignedLocations assignedLocation = new AssignedLocations();
		assignedLocation.setPlanId("cd09a3d4-01d9-485c-a1c5-a2eb078a61bf");
		assignedLocations.add(assignedLocation);
		List<Long> organizationalIds = Collections.singletonList(12233l);
		Practitioner practitioner = new Practitioner();
		practitioner.setIdentifier("practitioner-id");
		ImmutablePair<Practitioner, List<Long>> practitionerListImmutablePair = new ImmutablePair<>(practitioner,
				organizationalIds);

		List<org.opensrp.domain.postgres.PractitionerRole> practitionerRoles = new ArrayList<>();
		org.opensrp.domain.postgres.PractitionerRole practitionerRole = new org.opensrp.domain.postgres.PractitionerRole();
		practitionerRole.setIdentifier("practitioner-role-id");
		practitionerRole.setOrganizationId(12345l);
		practitionerRoles.add(practitionerRole);

		doReturn(practitionerRoles).when(practitionerRoleService).getPgRolesForPractitioner(anyString());
		doReturn(practitionerRoles).when(practitionerRoleRepository).getPgRolesForPractitioner(anyString());
		doReturn(practitioner).when(practitionerRepository).getPractitionerByUserId(anyString());
		doReturn(practitionerListImmutablePair).when(practitionerService).getOrganizationsByUserId(anyString());
		doReturn(assignedLocations).when(organizationService).findAssignedLocationsAndPlans(any(List.class));
		doReturn(assignedLocations).when(locationService).getAssignedLocations(anyString());
		boolean hasPermission = planPermissionEvaluator.hasObjectPermission(authentication, planDefinition,null);
		assertTrue(hasPermission);
	}

	@Test
	public void testHasObjectPermissionWithListOfPlanAsTargetObject() {
		PlanDefinition planDefinition = new PlanDefinition();
		planDefinition.setIdentifier("cd09a3d4-01d9-485c-a1c5-a2eb078a61bf");
		List<PlanDefinition> planDefinitions = new ArrayList<>();
		planDefinitions.add(planDefinition);
		planDefinitions.add(planDefinition);
		List<AssignedLocations> assignedLocations = new ArrayList<>();
		AssignedLocations assignedLocation = new AssignedLocations();
		assignedLocation.setPlanId("cd09a3d4-01d9-485c-a1c5-a2eb078a61bf");
		assignedLocation.setJurisdictionId("cd09a3d4-01d9-485c-a1c5-a2eb078a61bg");
		assignedLocations.add(assignedLocation);
		List<Long> organizationalIds = Collections.singletonList(12233l);
		Practitioner practitioner = new Practitioner();
		practitioner.setIdentifier("practitioner-id");
		ImmutablePair<Practitioner, List<Long>> practitionerListImmutablePair = new ImmutablePair<>(practitioner,
				organizationalIds);

		List<org.opensrp.domain.postgres.PractitionerRole> practitionerRoles = new ArrayList<>();
		org.opensrp.domain.postgres.PractitionerRole practitionerRole = new org.opensrp.domain.postgres.PractitionerRole();
		practitionerRole.setIdentifier("practitioner-role-id");
		practitionerRole.setOrganizationId(12345l);
		practitionerRoles.add(practitionerRole);

		doReturn(practitionerRoles).when(practitionerRoleService).getPgRolesForPractitioner(anyString());
		doReturn(practitionerRoles).when(practitionerRoleRepository).getPgRolesForPractitioner(anyString());
		doReturn(practitioner).when(practitionerRepository).getPractitionerByUserId(anyString());
		doReturn(practitionerListImmutablePair).when(practitionerService).getOrganizationsByUserId(anyString());
		doReturn(assignedLocations).when(organizationService).findAssignedLocationsAndPlans(any(List.class));
		doReturn(assignedLocations).when(locationService).getAssignedLocations(anyString());
		boolean hasPermission = planPermissionEvaluator.hasObjectPermission(authentication, (Serializable) planDefinitions,null);
		assertTrue(hasPermission);
	}
}
