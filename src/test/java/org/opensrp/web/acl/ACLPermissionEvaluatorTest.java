package org.opensrp.web.acl;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opensrp.domain.AssignedLocations;
import org.opensrp.domain.Organization;
import org.opensrp.repository.LocationRepository;
import org.opensrp.repository.PractitionerRepository;
import org.opensrp.repository.PractitionerRoleRepository;
import org.opensrp.service.OrganizationService;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PractitionerRoleService;
import org.opensrp.service.PractitionerService;
import org.smartregister.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ACLPermissionEvaluatorTest {

    @Mock
    private PlanPermissionEvaluator planPermissionEvaluator;

    @Mock
    private OrganizationPermissionEvaluator organizationPermissionEvaluator;

    @Mock
    private UserPermissionEvaluator userPermissionEvaluator;

    @Mock
    private LocationPermissionEvaluator locationPermissionEvaluator;

    @Mock
    private EventPermissionEvaluator eventPermissionEvaluator;

    @Mock
    private ClientPermissionEvaluator clientPermissionEvaluator;

    @Mock
    private Authentication authentication;

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

    @InjectMocks
    private ACLPermissionEvaluator aclPermissionEvaluator;

    private final List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");

    @Before
    public void setup() {
        initMocks(this);
        aclPermissionEvaluator = new ACLPermissionEvaluator();
        locationService.setLocationRepository(locationRepository);
        aclPermissionEvaluator.setPlanPermissionEvaluator(planPermissionEvaluator);
        aclPermissionEvaluator.setLocationPermissionEvaluator(locationPermissionEvaluator);
        aclPermissionEvaluator.setUserPermissionEvaluator(userPermissionEvaluator);
        aclPermissionEvaluator.setEventPermissionEvaluator(eventPermissionEvaluator);
        aclPermissionEvaluator.setClientPermissionEvaluator(clientPermissionEvaluator);
        aclPermissionEvaluator.setOrganizationPermissionEvaluator(organizationPermissionEvaluator);
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
    public void testHasPermissionWithPermissionFalse() {
        boolean hasPermission = aclPermissionEvaluator.hasPermission(authentication, null, "");
        assertFalse(hasPermission);
    }

    @Test
    public void testHasPermissionWithPlanDefinitionTargetObject() {
        List<AssignedLocations> assignedLocations = new ArrayList<>();
        PlanDefinition planDefinition = new PlanDefinition();
        List<Long> organizationalIds = new ArrayList<>();
        organizationalIds.add(12345l);
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
        doReturn(true).when(planPermissionEvaluator).hasPermission(authentication, planDefinition);
        boolean hasPermission = aclPermissionEvaluator.hasPermission(authentication, planDefinition, "USER");
        assertTrue(hasPermission);
    }

    @Test
    public void testHasPermissionWithOrganizationTargetObject() {
        Organization organization = new Organization();
        List<AssignedLocations> assignedLocations = new ArrayList<>();
        AssignedLocations assignedLocation = new AssignedLocations();
        assignedLocation.setOrganizationId("org-id");
        assignedLocations.add(assignedLocation);
        List<Long> organizationIds = new ArrayList<>();
        organizationIds.add(12345l);
        Practitioner practitioner = new Practitioner();
        practitioner.setIdentifier("practitioner-id");
        List<org.opensrp.domain.postgres.PractitionerRole> practitionerRoleList = new ArrayList<>();
        org.opensrp.domain.postgres.PractitionerRole practitionerRole = new org.opensrp.domain.postgres.PractitionerRole();
        practitionerRole.setOrganizationId(1234l);
        practitionerRoleList.add(practitionerRole);

        ImmutablePair<Practitioner, List<Long>> practitionerListImmutablePair = new ImmutablePair<>(practitioner,
                organizationIds);
        doReturn(practitionerRoleList).when(practitionerRoleService).getPgRolesForPractitioner(anyString());
        doReturn(practitioner).when(practitionerRepository).getPractitionerByUserId(anyString());
        doReturn(practitionerListImmutablePair).when(practitionerService).getOrganizationsByUserId(anyString());
        doReturn(assignedLocations).when(organizationService).findAssignedLocationsAndPlans(any(List.class));
        doReturn(assignedLocations).when(locationService).getAssignedLocations(anyString());
        doReturn(true).when(organizationPermissionEvaluator).hasPermission(authentication, organization);
        boolean hasPermission = aclPermissionEvaluator.hasPermission(authentication, organization, "USER");
        assertTrue(hasPermission);
    }

    @Test
    public void testHasPermissionWithPhysicalLocationTargetObject() {
        PhysicalLocation physicalLocation = new PhysicalLocation();
        List<AssignedLocations> assignedLocations = new ArrayList<>();
        AssignedLocations assignedLocation = new AssignedLocations();
        assignedLocation.setOrganizationId("org-id");
        assignedLocations.add(assignedLocation);
        List<Long> organizationIds = new ArrayList<>();
        organizationIds.add(12345l);
        Practitioner practitioner = new Practitioner();
        practitioner.setIdentifier("practitioner-id");
        List<org.opensrp.domain.postgres.PractitionerRole> practitionerRoleList = new ArrayList<>();
        org.opensrp.domain.postgres.PractitionerRole practitionerRole = new org.opensrp.domain.postgres.PractitionerRole();
        practitionerRole.setOrganizationId(1234l);
        practitionerRoleList.add(practitionerRole);
        ImmutablePair<Practitioner, List<Long>> practitionerListImmutablePair = new ImmutablePair<>(practitioner,
                organizationIds);
        doReturn(practitionerRoleList).when(practitionerRoleService).getPgRolesForPractitioner(anyString());
        doReturn(practitioner).when(practitionerRepository).getPractitionerByUserId(anyString());
        doReturn(practitionerListImmutablePair).when(practitionerService).getOrganizationsByUserId(anyString());
        doReturn(assignedLocations).when(organizationService).findAssignedLocationsAndPlans(any(List.class));
        doReturn(assignedLocations).when(locationService).getAssignedLocations(anyString());
        doReturn(true).when(locationPermissionEvaluator).hasPermission(authentication, physicalLocation);
        boolean hasPermission = aclPermissionEvaluator.hasPermission(authentication, physicalLocation, "USER");
        assertTrue(hasPermission);
    }

    @Test
    public void testHasPermissionWithEventTargetObject() {
        Event event = new Event();
        List<AssignedLocations> assignedLocations = new ArrayList<>();
        AssignedLocations assignedLocation = new AssignedLocations();
        assignedLocation.setOrganizationId("org-id");
        assignedLocations.add(assignedLocation);
        List<Long> organizationIds = new ArrayList<>();
        organizationIds.add(12345l);
        Practitioner practitioner = new Practitioner();
        practitioner.setIdentifier("practitioner-id");
        List<org.opensrp.domain.postgres.PractitionerRole> practitionerRoleList = new ArrayList<>();
        org.opensrp.domain.postgres.PractitionerRole practitionerRole = new org.opensrp.domain.postgres.PractitionerRole();
        practitionerRole.setOrganizationId(1234l);
        practitionerRoleList.add(practitionerRole);
        ImmutablePair<Practitioner, List<Long>> practitionerListImmutablePair = new ImmutablePair<>(practitioner,
                organizationIds);
        doReturn(practitionerRoleList).when(practitionerRoleService).getPgRolesForPractitioner(anyString());
        doReturn(practitioner).when(practitionerRepository).getPractitionerByUserId(anyString());
        doReturn(practitionerListImmutablePair).when(practitionerService).getOrganizationsByUserId(anyString());
        doReturn(assignedLocations).when(organizationService).findAssignedLocationsAndPlans(any(List.class));
        doReturn(assignedLocations).when(locationService).getAssignedLocations(anyString());
        doReturn(true).when(eventPermissionEvaluator).hasPermission(authentication, event);
        boolean hasPermission = aclPermissionEvaluator.hasPermission(authentication, event, "USER");
        assertTrue(hasPermission);
    }

    @Test
    public void testHasPermissionWithClientTargetObject() {
        Client client = new Client("base-entity-id");
        List<AssignedLocations> assignedLocations = new ArrayList<>();
        AssignedLocations assignedLocation = new AssignedLocations();
        assignedLocation.setOrganizationId("org-id");
        assignedLocations.add(assignedLocation);
        List<Long> organizationIds = new ArrayList<>();
        organizationIds.add(12345l);
        Practitioner practitioner = new Practitioner();
        practitioner.setIdentifier("practitioner-id");
        List<org.opensrp.domain.postgres.PractitionerRole> practitionerRoleList = new ArrayList<>();
        org.opensrp.domain.postgres.PractitionerRole practitionerRole = new org.opensrp.domain.postgres.PractitionerRole();
        practitionerRole.setOrganizationId(1234l);
        practitionerRoleList.add(practitionerRole);
        ImmutablePair<Practitioner, List<Long>> practitionerListImmutablePair = new ImmutablePair<>(practitioner,
                organizationIds);
        doReturn(practitionerRoleList).when(practitionerRoleService).getPgRolesForPractitioner(anyString());
        doReturn(practitioner).when(practitionerRepository).getPractitionerByUserId(anyString());
        doReturn(practitionerListImmutablePair).when(practitionerService).getOrganizationsByUserId(anyString());
        doReturn(assignedLocations).when(organizationService).findAssignedLocationsAndPlans(any(List.class));
        doReturn(assignedLocations).when(locationService).getAssignedLocations(anyString());
        doReturn(true).when(clientPermissionEvaluator).hasPermission(authentication, client);
        boolean hasPermission = aclPermissionEvaluator.hasPermission(authentication, client, "USER");
        assertTrue(hasPermission);
    }

    @Test
    public void testHasPermissionWithUserTargetObject() {
        Object object = "user";
        doReturn(true).when(userPermissionEvaluator).hasPermission(authentication, "user");
        boolean hasPermission = aclPermissionEvaluator.hasPermission(authentication, object, "USER");
        assertTrue(hasPermission);
    }

    @Test
    public void testHasPermissionV2WithUserAsTargetObject() {
        User user = new User(UUID.randomUUID().toString()).withRoles(roles).withUsername("test_user1");
        doReturn(true).when(userPermissionEvaluator).hasObjectPermission(authentication, user, "USER");
        boolean hasPermission = aclPermissionEvaluator.hasPermission(authentication, user, "User", "USER");
        assertTrue(hasPermission);
    }

    @Test
    public void testHasPermissionV2WithEventAsTargetObject() {
        Event event = new Event();
        event.setLocationId("cd09a3d4-01d9-485c-a1c5-a2eb078a61bf");
        event.setTeamId("cd09a3d4-01d9-485c-a1c5-a2eb078a61b2");
        List<AssignedLocations> assignedLocations = new ArrayList<>();
        AssignedLocations assignedLocation = new AssignedLocations();
        assignedLocation.setJurisdictionId("cd09a3d4-01d9-485c-a1c5-a2eb078a61bf");
        assignedLocation.setOrganizationId("cd09a3d4-01d9-485c-a1c5-a2eb078a61b2");
        assignedLocations.add(assignedLocation);
        List<Long> organizationIds = new ArrayList<>();
        organizationIds.add(12345l);
        Practitioner practitioner = new Practitioner();
        practitioner.setIdentifier("practitioner-id");
        List<org.opensrp.domain.postgres.PractitionerRole> practitionerRoleList = new ArrayList<>();
        org.opensrp.domain.postgres.PractitionerRole practitionerRole = new org.opensrp.domain.postgres.PractitionerRole();
        practitionerRole.setOrganizationId(1234l);
        practitionerRoleList.add(practitionerRole);
        ImmutablePair<Practitioner, List<Long>> practitionerListImmutablePair = new ImmutablePair<>(practitioner,
                organizationIds);
        doReturn(practitionerRoleList).when(practitionerRoleService).getPgRolesForPractitioner(anyString());
        doReturn(practitioner).when(practitionerRepository).getPractitionerByUserId(anyString());
        doReturn(practitionerListImmutablePair).when(practitionerService).getOrganizationsByUserId(anyString());
        doReturn(assignedLocations).when(organizationService).findAssignedLocationsAndPlans(any(List.class));
        doReturn(assignedLocations).when(locationService).getAssignedLocations(anyString());
        doReturn(true).when(eventPermissionEvaluator).hasObjectPermission(authentication, event, "USER");
        boolean hasPermission = aclPermissionEvaluator.hasPermission(authentication, event, "Event", "USER");
        assertTrue(hasPermission);
    }

    @Test
    public void testHasPermissionV2WithClientAsTargetObject() {
        Client client = new Client("base-entity-id");
        client.setLocationId("cd09a3d4-01d9-485c-a1c5-a2eb078a61bf");
        client.setTeamId("cd09a3d4-01d9-485c-a1c5-a2eb078a61b2");
        List<AssignedLocations> assignedLocations = new ArrayList<>();
        AssignedLocations assignedLocation = new AssignedLocations();
        assignedLocation.setJurisdictionId("cd09a3d4-01d9-485c-a1c5-a2eb078a61bf");
        assignedLocation.setOrganizationId("cd09a3d4-01d9-485c-a1c5-a2eb078a61b2");
        assignedLocations.add(assignedLocation);
        List<Long> organizationIds = new ArrayList<>();
        organizationIds.add(12345l);
        Practitioner practitioner = new Practitioner();
        practitioner.setIdentifier("practitioner-id");
        List<org.opensrp.domain.postgres.PractitionerRole> practitionerRoleList = new ArrayList<>();
        org.opensrp.domain.postgres.PractitionerRole practitionerRole = new org.opensrp.domain.postgres.PractitionerRole();
        practitionerRole.setOrganizationId(1234l);
        practitionerRoleList.add(practitionerRole);
        ImmutablePair<Practitioner, List<Long>> practitionerListImmutablePair = new ImmutablePair<>(practitioner,
                organizationIds);
        doReturn(practitionerRoleList).when(practitionerRoleService).getPgRolesForPractitioner(anyString());
        doReturn(practitioner).when(practitionerRepository).getPractitionerByUserId(anyString());
        doReturn(practitionerListImmutablePair).when(practitionerService).getOrganizationsByUserId(anyString());
        doReturn(assignedLocations).when(organizationService).findAssignedLocationsAndPlans(any(List.class));
        doReturn(assignedLocations).when(locationService).getAssignedLocations(anyString());
        doReturn(true).when(clientPermissionEvaluator).hasObjectPermission(authentication, client, "USER");
        boolean hasPermission = aclPermissionEvaluator.hasPermission(authentication, client, "Client", "USER");
        assertTrue(hasPermission);
    }
}
