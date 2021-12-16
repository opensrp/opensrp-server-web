package org.opensrp.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.opensrp.common.AllConstants.BaseEntity.SERVER_VERSIOIN;
import static org.opensrp.web.rest.PlanResource.OPERATIONAL_AREA_ID;
import static org.opensrp.web.rest.PlanResource.gson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensrp.domain.LocationDetail;
import org.opensrp.domain.postgres.PlanProcessingStatus;
import org.opensrp.domain.PlanTaskCount;
import org.opensrp.domain.TaskCount;
import org.opensrp.domain.Template;
import org.opensrp.search.PlanSearchBean;
import org.opensrp.service.EventService;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PlanProcessingStatusService;
import org.opensrp.service.PlanService;
import org.opensrp.service.TemplateService;
import org.opensrp.util.constants.PlanConstants;
import org.opensrp.util.constants.PlanProcessingStatusConstants;
import org.opensrp.web.Constants;
import org.opensrp.web.bean.Identifier;
import org.smartregister.domain.Event;
import org.smartregister.domain.Jurisdiction;
import org.smartregister.domain.PhysicalLocation;
import org.smartregister.domain.PlanDefinition;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Vincent Karuri on 06/05/2019
 */
public class PlanResourceTest extends BaseSecureResourceTest<PlanDefinition> {
	
	@Rule
	public MockitoRule rule = MockitoJUnit.rule();
	
	private final static String BASE_URL = "/rest/plans/";

	private PlanService planService;

	private PhysicalLocationService locationService;
	
	private final String plansJson = "{\n" + "  \"identifier\": \"plan_1\",\n" + "  \"version\": \"\",\n"
	        + "  \"name\": \"\",\n" + "  \"title\": \"\",\n" + "  \"status\": \"\",\n" + "  \"date\": \"2019-04-10\",\n"
	        + "  \"effectivePeriod\": {\n" + "    \"start\": \"2019-04-10\",\n" + "    \"end\": \"2019-04-10\"\n" + "  },\n"
	        + "  \"useContext\": [\n" + "    {\n" + "      \"code\": \"\",\n" + "      \"valueCodableConcept\": \"\"\n"
	        + "    },\n" + "    {\n" + "      \"code\": \"\",\n" + "      \"valueCodableConcept\": \"\"\n" + "    }\n"
	        + "  ],\n" + "  \"jurisdiction\": [\n" + "    {\n" + "      \"code\": \"operational_area_1\"\n" + "    }\n"
	        + "  ],\n" + "  \"goal\": [\n" + "    {\n" + "      \"id\": \"\",\n" + "      \"description\": \"\",\n"
	        + "      \"priority\": 1,\n" + "      \"target\": [\n" + "        {\n" + "          \"measure\": \"\",\n"
	        + "          \"detail\": {\n" + "            \"detailQuantity\": {\n" + "              \"value\": 8,\n"
	        + "              \"comparator\": \"\",\n" + "              \"unit\": \"\"\n" + "            },\n"
	        + "            \"detailRange\": {\n" + "              \"high\": {\n" + "                \"value\": 0.2,\n"
	        + "                \"comparator\": \"\",\n" + "                \"unit\": \"\"\n" + "              },\n"
	        + "              \"low\": {\n" + "                \"value\": 0.2,\n" + "                \"comparator\": \"\",\n"
	        + "                \"unit\": \"\"\n" + "              }\n" + "            },\n"
	        + "            \"detailCodableConcept\": {\n" + "              \"text\": \"\"\n" + "            }\n"
	        + "          },\n" + "          \"due\": \"2019-04-10\"\n" + "        }\n" + "      ]\n" + "    }\n" + "  ],\n"
	        + "  \"action\": [\n" + "    {\n" + "      \"identifier\": \"\",\n" + "      \"prefix\": 1,\n"
	        + "      \"title\": \"\",\n" + "      \"description\": \"\",\n" + "      \"code\": \"\",\n"
	        + "      \"timingPeriod\": {\n" + "        \"start\": \"2019-04-10\",\n" + "        \"end\": \"2019-04-10\"\n"
	        + "      },\n" + "      \"reason\": \"\",\n" + "      \"goalId\": \"\",\n"
	        + "      \"subjectCodableConcept\": {\n" + "        \"text\": \"\"\n" + "      },\n"
	        + "      \"taskTemplate\": \"\"\n" + "    }\n" + "  ],\n" + "  \"serverVersion\": 0\n" + "}";

	private final String templateString = "{\n" +
			"    \"templateId\": 1,\n" +
			"    \"template\": {\n" +
			"        \"identifier\": \"${planIdentifier}\",\n" +
			"        \"version\": \"1\",\n" +
			"        \"name\": \"${focus_status}-${focus_name}-${patient_name}_${patient_surname}-${currentDate}-${flag}\",\n" +
			"        \"title\": \"${focus_status} - ${focus_name} - ${patient_name} ${patient_surname} - ${currentDate} - ${flag}\",\n" +
			"        \"status\": \"draft\",\n" +
			"        \"date\": \"${currentDate}\",\n" +
			"        \"effectivePeriod\": {\n" +
			"            \"start\": \"${currentDate}\",\n" +
			"            \"end\": \"${endDate}\"\n" +
			"        },\n" +
			"        \"useContext\": [\n" +
			"            {\n" +
			"                \"code\": \"interventionType\",\n" +
			"                \"valueCodableConcept\": \"Dynamic-FI\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"code\": \"fiStatus\",\n" +
			"                \"valueCodableConcept\": \"${focus_status}\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"code\": \"fiReason\",\n" +
			"                \"valueCodableConcept\": \"Case Triggered\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"code\": \"opensrpEventId\",\n" +
			"                \"valueCodableConcept\": \"${opensrpCaseClassificationEventId}\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"code\": \"caseNum\",\n" +
			"                \"valueCodableConcept\": \"${case_number}\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"code\": \"taskGenerationStatus\",\n" +
			"                \"valueCodableConcept\": \"internal\"\n" +
			"            }\n" +
			"        ],\n" +
			"        \"jurisdiction\": [\n" +
			"            {\n" +
			"                \"code\": \"${focus_id}\"\n" +
			"            }\n" +
			"        ],\n" +
			"        \"serverVersion\": 0,\n" +
			"        \"experimental\": false,\n" +
			"        \"goal\": [\n" +
			"            {\n" +
			"                \"id\": \"Case_Confirmation\",\n" +
			"                \"description\": \"ยืนยันบ้านผู้ป่วย\",\n" +
			"                \"priority\": \"medium-priority\",\n" +
			"                \"target\": [\n" +
			"                    {\n" +
			"                        \"measure\": \"จำนวนผู้ป่วยที่ได้รับการยืนยัน\",\n" +
			"                        \"detail\": {\n" +
			"                            \"detailQuantity\": {\n" +
			"                                \"value\": 1.0,\n" +
			"                                \"comparator\": \"&amp;gt;=\",\n" +
			"                                \"unit\": \"case(s)\"\n" +
			"                            }\n" +
			"                        },\n" +
			"                        \"due\": \"${endDate}\"\n" +
			"                    }\n" +
			"                ]\n" +
			"            },\n" +
			"            {\n" +
			"                \"id\": \"RACD_register_families\",\n" +
			"                \"description\": \"ลงทะเบียนครัวเรือนและสมาชิกในครัวเรือน (100%) ภายในพื้นที่ปฏิบัติงาน\",\n" +
			"                \"priority\": \"medium-priority\",\n" +
			"                \"target\": [\n" +
			"                    {\n" +
			"                        \"measure\": \"ร้อยละของบ้าน สิ่งปลูกสร้างที่ได้ลงทะเบียนข้อมูลครัวเรือน\",\n" +
			"                        \"detail\": {\n" +
			"                            \"detailQuantity\": {\n" +
			"                                \"value\": 100.0,\n" +
			"                                \"comparator\": \"&gt;=\",\n" +
			"                                \"unit\": \"%\"\n" +
			"                            }\n" +
			"                        },\n" +
			"                        \"due\": \"${endDate}\"\n" +
			"                    }\n" +
			"                ]\n" +
			"            },\n" +
			"            {\n" +
			"                \"id\": \"RACD_Blood_Screening\",\n" +
			"                \"description\": \"เจาะเลือดรอบบ้านผู้ป่วยในรัศมี 1 กิโลเมตร (100%)\",\n" +
			"                \"priority\": \"medium-priority\",\n" +
			"                \"target\": [\n" +
			"                    {\n" +
			"                        \"measure\": \"จำนวนผู้ที่ได้รับการเจาะโลหิต\",\n" +
			"                        \"detail\": {\n" +
			"                            \"detailQuantity\": {\n" +
			"                                \"value\": 50.0,\n" +
			"                                \"comparator\": \"&gt;=\",\n" +
			"                                \"unit\": \"คน\"\n" +
			"                            }\n" +
			"                        },\n" +
			"                        \"due\": \"${endDate}\"\n" +
			"                    }\n" +
			"                ]\n" +
			"            },\n" +
			"            {\n" +
			"                \"id\": \"BCC_Focus\",\n" +
			"                \"description\": \"ให้สุขศึกษาในพื้นที่ปฏิบัติงานอย่างน้อย 1 ครั้ง\",\n" +
			"                \"priority\": \"medium-priority\",\n" +
			"                \"target\": [\n" +
			"                    {\n" +
			"                        \"measure\": \"จำนวนกิจกรรมการให้สุขศึกษา\",\n" +
			"                        \"detail\": {\n" +
			"                            \"detailQuantity\": {\n" +
			"                                \"value\": 1.0,\n" +
			"                                \"comparator\": \"&gt;=\",\n" +
			"                                \"unit\": \"แห่ง\"\n" +
			"                            }\n" +
			"                        },\n" +
			"                        \"due\": \"${endDate}\"\n" +
			"                    }\n" +
			"                ]\n" +
			"            },\n" +
			"            {\n" +
			"                \"id\": \"RACD_bednet_distribution\",\n" +
			"                \"description\": \"แจกมุ้งทุกหลังคาเรือนในพื้นที่ปฏิบัติงาน (100%)\",\n" +
			"                \"priority\": \"medium-priority\",\n" +
			"                \"target\": [\n" +
			"                    {\n" +
			"                        \"measure\": \"จำนวนบ้าน/สิ่งปลูกสร้างที่ได้รับมุ้ง\",\n" +
			"                        \"detail\": {\n" +
			"                            \"detailQuantity\": {\n" +
			"                                \"value\": 90.0,\n" +
			"                                \"comparator\": \"&gt;=\",\n" +
			"                                \"unit\": \"%\"\n" +
			"                            }\n" +
			"                        },\n" +
			"                        \"due\": \"${endDate}\"\n" +
			"                    }\n" +
			"                ]\n" +
			"            },\n" +
			"            {\n" +
			"                \"id\": \"Larval_Dipping\",\n" +
			"                \"description\": \"ดำเนินกิจกรรมจับลูกน้ำอย่างน้อย 3 แห่งในพื้นที่ปฏิบัติงาน\",\n" +
			"                \"priority\": \"medium-priority\",\n" +
			"                \"target\": [\n" +
			"                    {\n" +
			"                        \"measure\": \"จำนวนกิจกรรมการตักลูกน้ำ\",\n" +
			"                        \"detail\": {\n" +
			"                            \"detailQuantity\": {\n" +
			"                                \"value\": 3.0,\n" +
			"                                \"comparator\": \"&gt;=\",\n" +
			"                                \"unit\": \"แห่ง\"\n" +
			"                            }\n" +
			"                        },\n" +
			"                        \"due\": \"${endDate}\"\n" +
			"                    }\n" +
			"                ]\n" +
			"            },\n" +
			"            {\n" +
			"                \"id\": \"Mosquito_Collection\",\n" +
			"                \"description\": \"กิจกรรมจับยุงกำหนดไว้อย่างน้อย 3 แห่ง\",\n" +
			"                \"priority\": \"medium-priority\",\n" +
			"                \"target\": [\n" +
			"                    {\n" +
			"                        \"measure\": \"จำนวนกิจกรรมการจับยุง\",\n" +
			"                        \"detail\": {\n" +
			"                            \"detailQuantity\": {\n" +
			"                                \"value\": 3.0,\n" +
			"                                \"comparator\": \"&gt;=\",\n" +
			"                                \"unit\": \"แห่ง\"\n" +
			"                            }\n" +
			"                        },\n" +
			"                        \"due\": \"${endDate}\"\n" +
			"                    }\n" +
			"                ]\n" +
			"            }\n" +
			"        ],\n" +
			"        \"action\": [\n" +
			"            {\n" +
			"                \"identifier\": \"662d4bff-43cc-4f50-af1e-3bc86f8af253\",\n" +
			"                \"prefix\": 1,\n" +
			"                \"title\": \"การยืนยันบ้านผู้ป่วย\",\n" +
			"                \"description\": \"ยืนยันบ้านผู้ป่วย\",\n" +
			"                \"code\": \"Case Confirmation\",\n" +
			"                \"timingPeriod\": {\n" +
			"                    \"start\": \"2021-09-13\",\n" +
			"                    \"end\": \"2021-09-23\"\n" +
			"                },\n" +
			"                \"reason\": \"Investigation\",\n" +
			"                \"goalId\": \"Case_Confirmation\",\n" +
			"                \"subjectCodableConcept\": {\n" +
			"                    \"text\": \"Jurisdiction\"\n" +
			"                },\n" +
			"                \"trigger\": [\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"plan-activation\"\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"condition\": [\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Event is case details event\",\n" +
			"                            \"expression\": \"questionnaire = 'Case_Details'\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"definitionUri\": \"case_confirmation.json\",\n" +
			"                \"type\": \"create\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"identifier\": \"${registerFamilyActionId}\",\n" +
			"                \"prefix\": 2,\n" +
			"                \"title\": \"ลงทะเบียนครัวเรือน\",\n" +
			"                \"description\": \"ลงทะเบียนครัวเรือนและสมาชิกในครัวเรือน (100%) ภายในพื้นที่ปฏิบัติงาน\",\n" +
			"                \"code\": \"RACD Register Family\",\n" +
			"                \"timingPeriod\": {\n" +
			"                    \"start\": \"${currentDate}\",\n" +
			"                    \"end\": \"${endDate}\"\n" +
			"                },\n" +
			"                \"reason\": \"Investigation\",\n" +
			"                \"goalId\": \"RACD_register_families\",\n" +
			"                \"subjectCodableConcept\": {\n" +
			"                    \"text\": \"Location\"\n" +
			"                },\n" +
			"                \"trigger\": [\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"event-submission\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Trigger when a Register_Structure event is submitted\",\n" +
			"                            \"expression\": \"questionnaire = 'Register_Structure' or questionnaire = 'Archive_Family'\"\n" +
			"                        }\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"plan-activation\"\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"condition\": [\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Apply to residential structures in Register_Structure questionnaires\",\n" +
			"                            \"expression\": \"$this.is(FHIR.Location) or (questionnaire = 'Register_Structure' and $this.item.where(linkId='structureType').answer.value ='Residential Structure')\"\n" +
			"                        }\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Structure is residential or type does not exist\",\n" +
			"                            \"expression\": \"$this.is(FHIR.QuestionnaireResponse) or (($this.type.where(id='locationType').exists().not() or $this.type.where(id='locationType').text = 'Residential Structure') and $this.contained.exists().not())\",\n" +
			"                            \"subjectCodableConcept\": {\n" +
			"                                \"text\": \"Family\"\n" +
			"                            }\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"definitionUri\": \"family_register.json\",\n" +
			"                \"type\": \"create\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"identifier\": \"${bloodScreeningActionId}\",\n" +
			"                \"prefix\": 3,\n" +
			"                \"title\": \"กิจกรรมการเจาะโลหิต\",\n" +
			"                \"description\": \"เจาะเลือดรอบบ้านผู้ป่วยในรัศมี 1 กิโลเมตร (100%)\",\n" +
			"                \"code\": \"Blood Screening\",\n" +
			"                \"timingPeriod\": {\n" +
			"                    \"start\": \"${currentDate}\",\n" +
			"                    \"end\": \"${endDate}\"\n" +
			"                },\n" +
			"                \"reason\": \"Investigation\",\n" +
			"                \"goalId\": \"RACD_Blood_Screening\",\n" +
			"                \"subjectCodableConcept\": {\n" +
			"                    \"text\": \"Person\"\n" +
			"                },\n" +
			"                \"trigger\": [\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"plan-activation\"\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"event-submission\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Trigger when a Family Registration or Family Member Registration event is submitted\",\n" +
			"                            \"expression\": \"questionnaire = 'Family_Registration' or questionnaire = 'Family_Member_Registration'\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"condition\": [\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Person is older than 5 years or person associated with questionnaire response if older than 5 years\",\n" +
			"                            \"expression\": \"($this.is(FHIR.Patient) and $this.birthDate &lt;= today() - 5 'years') or ($this.contained.where(Patient.birthDate &lt;= today() - 5 'years').exists())\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"definitionUri\": \"blood_screening.json\",\n" +
			"                \"type\": \"create\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"identifier\": \"${bccActionId}\",\n" +
			"                \"prefix\": 4,\n" +
			"                \"title\": \"กิจกรรมการให้สุขศึกษา\",\n" +
			"                \"description\": \"ดำเนินกิจกรรมให้สุขศึกษา\",\n" +
			"                \"code\": \"BCC\",\n" +
			"                \"timingPeriod\": {\n" +
			"                    \"start\": \"${currentDate}\",\n" +
			"                    \"end\": \"${endDate}\"\n" +
			"                },\n" +
			"                \"reason\": \"Investigation\",\n" +
			"                \"goalId\": \"BCC_Focus\",\n" +
			"                \"subjectCodableConcept\": {\n" +
			"                    \"text\": \"Jurisdiction\"\n" +
			"                },\n" +
			"                \"trigger\": [\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"plan-activation\"\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"condition\": [\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Jurisdiction type location\",\n" +
			"                            \"expression\": \"Location.physicalType.coding.exists(code='jdn')\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"definitionUri\": \"behaviour_change_communication.json\",\n" +
			"                \"type\": \"create\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"identifier\": \"${bednetDistributionActionId}\",\n" +
			"                \"prefix\": 5,\n" +
			"                \"title\": \"กิจกรรมสำรวจ/ชุบ/แจกมุ้ง\",\n" +
			"                \"description\": \"แจกมุ้งทุกหลังคาเรือนในพื้นที่ปฏิบัติงาน (100%)\",\n" +
			"                \"code\": \"Bednet Distribution\",\n" +
			"                \"timingPeriod\": {\n" +
			"                    \"start\": \"${currentDate}\",\n" +
			"                    \"end\": \"${endDate}\"\n" +
			"                },\n" +
			"                \"reason\": \"Investigation\",\n" +
			"                \"goalId\": \"RACD_bednet_distribution\",\n" +
			"                \"subjectCodableConcept\": {\n" +
			"                    \"text\": \"Location\"\n" +
			"                },\n" +
			"                \"trigger\": [\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"plan-activation\"\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"event-submission\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Trigger when a Family Registration event is submitted\",\n" +
			"                            \"expression\": \"questionnaire = 'Family_Registration'\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"condition\": [\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Structure is residential or type does not exist\",\n" +
			"                            \"expression\": \"$this.is(FHIR.QuestionnaireResponse) or (($this.type.where(id='locationType').exists().not() or $this.type.where(id='locationType').text = 'Residential Structure') and $this.contained.exists())\",\n" +
			"                            \"subjectCodableConcept\": {\n" +
			"                                \"text\": \"Family\"\n" +
			"                            }\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"definitionUri\": \"bednet_distribution.json\",\n" +
			"                \"type\": \"create\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"identifier\": \"${larvalDippingActionId}\",\n" +
			"                \"prefix\": 6,\n" +
			"                \"title\": \"กิจกรรมการตักลูกน้ำ\",\n" +
			"                \"description\": \"ดำเนินกิจกรรมจับลูกน้ำอย่างน้อย 3 แห่งในพื้นที่ปฏิบัติงาน\",\n" +
			"                \"code\": \"Larval Dipping\",\n" +
			"                \"timingPeriod\": {\n" +
			"                    \"start\": \"${currentDate}\",\n" +
			"                    \"end\": \"${endDate}\"\n" +
			"                },\n" +
			"                \"reason\": \"Investigation\",\n" +
			"                \"goalId\": \"Larval_Dipping\",\n" +
			"                \"subjectCodableConcept\": {\n" +
			"                    \"text\": \"Location\"\n" +
			"                },\n" +
			"                \"trigger\": [\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"event-submission\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Trigger when a Register_Structure event is submitted\",\n" +
			"                            \"expression\": \"questionnaire = 'Register_Structure'\"\n" +
			"                        }\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"plan-activation\"\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"condition\": [\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Apply to larval breeding sites in Register_Structure questionnaires\",\n" +
			"                            \"expression\": \"$this.is(FHIR.Location) or (questionnaire = 'Register_Structure' and $this.item.where(linkId='structureType').answer.value ='Larval Breeding Site')\"\n" +
			"                        }\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Structure is a larval breeding site\",\n" +
			"                            \"expression\": \"$this.is(FHIR.QuestionnaireResponse) or $this.type.where(id='locationType').text = 'Larval Breeding Site'\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"definitionUri\": \"larval_dipping_form.json\",\n" +
			"                \"type\": \"create\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"identifier\": \"${mosquitoCollectionActionId}\",\n" +
			"                \"prefix\": 7,\n" +
			"                \"title\": \"กิจกรรมการจับยุง\",\n" +
			"                \"description\": \"กิจกรรมจับยุงกำหนดไว้อย่างน้อย 3 แห่ง\",\n" +
			"                \"code\": \"Mosquito Collection\",\n" +
			"                \"timingPeriod\": {\n" +
			"                    \"start\": \"${currentDate}\",\n" +
			"                    \"end\": \"${endDate}\"\n" +
			"                },\n" +
			"                \"reason\": \"Investigation\",\n" +
			"                \"goalId\": \"Mosquito_Collection\",\n" +
			"                \"subjectCodableConcept\": {\n" +
			"                    \"text\": \"Location\"\n" +
			"                },\n" +
			"                \"trigger\": [\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"plan-activation\"\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"event-submission\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Trigger when a Register_Structure event is submitted\",\n" +
			"                            \"expression\": \"questionnaire = 'Register_Structure'\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"condition\": [\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Apply to mosquito collection point in Register_Structure questionnaires\",\n" +
			"                            \"expression\": \"$this.is(FHIR.Location) or (questionnaire = 'Register_Structure' and $this.item.where(linkId='structureType').answer.value ='Mosquito Collection Point')\"\n" +
			"                        }\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Structure is a mosquito collection point\",\n" +
			"                            \"expression\": \"$this.is(FHIR.QuestionnaireResponse) or $this.type.where(id='locationType').text = 'Mosquito Collection Point'\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"definitionUri\": \"mosquito_collection_form.json\",\n" +
			"                \"type\": \"create\"\n" +
			"            }\n" +
			"        ]\n" +
			"    },\n" +
			"    \"type\": \"plan\",\n" +
			"    \"version\": 11\n" +
			"}";

	private final String caseDetailsEventJson = "{\n" +
			"  \"type\": \"Event\",\n" +
			"  \"dateCreated\": \"2021-12-07T10:14:18.721Z\",\n" +
			"  \"serverVersion\": 0,\n" +
			"  \"identifiers\": {},\n" +
			"  \"baseEntityId\": \"b49faa39-5e24-4de8-bf3f-cdda6f320179\",\n" +
			"  \"locationId\": \"b49faa39-5e24-4de8-bf3f-cdda6f320179\",\n" +
			"  \"eventDate\": \"2021-12-07T00:00:00.000Z\",\n" +
			"  \"eventType\": \"Case_Details\",\n" +
			"  \"formSubmissionId\": \"5fecc570-dbfa-43cb-a7b5-60c54d5678ba\",\n" +
			"  \"providerId\": \"nifi-user\",\n" +
			"  \"duration\": 0,\n" +
			"  \"obs\": [],\n" +
			"  \"entityType\": \"Case_Details\",\n" +
			"  \"details\": {\n" +
			"    \"id\": \"3761\",\n" +
			"    \"age\": \"69\",\n" +
			"    \"bfid\": \"1206030007\",\n" +
			"    \"flag\": \"Site\",\n" +
			"    \"species\": \"M\",\n" +
			"    \"surname\": \"QA Test\",\n" +
			"    \"focus_id\": \"b49faa39-5e24-4de8-bf3f-cdda6f320179\",\n" +
			"    \"first_name\": \"Madrine\",\n" +
			"    \"focus_name\": \"เมืองทอง(Site)-4\",\n" +
			"    \"case_number\": \"2401224081141261323\",\n" +
			"    \"family_name\": \"Madrine\",\n" +
			"    \"focus_reason\": \"Investigation\",\n" +
			"    \"focus_status\": \"A1\",\n" +
			"    \"house_number\": \"H321\",\n" +
			"    \"planIdentifier\": \"db01cf96-5975-11ec-bf63-0242ac130002\",\n" +
			"    \"ep1_create_date\": \"2021-12-07T00:00:00.000+0000\",\n" +
			"    \"ep3_create_date\": \"2021-12-07T00:00:00.000+0000\",\n" +
			"    \"investigtion_date\": \"2021-12-07T00:00:00.000+0000\",\n" +
			"    \"case_classification\": \"Bz\"\n" +
			"  },\n" +
			"  \"version\": 1557860282617,\n" +
			"  \"teamId\": \" \",\n" +
			"  \"_id\": \"32b4da1a-297e-47c2-9bdf-7e35b0e1cc73\",\n" +
			"  \"_rev\": \"v1\"\n" +
			"}";

	private final String caseTriggeredPlanJson = "{\n" +
			"        \"identifier\": \"db01cf96-5975-11ec-bf63-0242ac130002\",\n" +
			"        \"version\": \"1\",\n" +
			"        \"name\": \"A1-เมืองทอง(Site)-4-Madrine_QA Test-2021-12-10-Site\",\n" +
			"        \"title\": \"A1 - เมืองทอง(Site)-4 - Madrine QA Test - 2021-12-10 - Site\",\n" +
			"        \"status\": \"draft\",\n" +
			"        \"date\": \"2021-12-10\",\n" +
			"        \"effectivePeriod\": {\n" +
			"            \"start\": \"2021-12-10\",\n" +
			"            \"end\": \"2026-12-10\"\n" +
			"        },\n" +
			"        \"useContext\": [\n" +
			"            {\n" +
			"                \"code\": \"interventionType\",\n" +
			"                \"valueCodableConcept\": \"Dynamic-FI\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"code\": \"fiStatus\",\n" +
			"                \"valueCodableConcept\": \"A1\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"code\": \"fiReason\",\n" +
			"                \"valueCodableConcept\": \"Case Triggered\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"code\": \"opensrpEventId\",\n" +
			"                \"valueCodableConcept\": \"4a74b717-db86-44e4-93da-e930248c87c7\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"code\": \"caseNum\",\n" +
			"                \"valueCodableConcept\": \"2401224081141261323\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"code\": \"taskGenerationStatus\",\n" +
			"                \"valueCodableConcept\": \"internal\"\n" +
			"            }\n" +
			"        ],\n" +
			"        \"jurisdiction\": [\n" +
			"            {\n" +
			"                \"code\": \"b49faa39-5e24-4de8-bf3f-cdda6f320179\"\n" +
			"            }\n" +
			"        ],\n" +
			"        \"serverVersion\": 1600354181850,\n" +
			"        \"goal\": [\n" +
			"            {\n" +
			"                \"id\": \"Case_Confirmation\",\n" +
			"                \"description\": \"ยืนยันบ้านผู้ป่วย\",\n" +
			"                \"priority\": \"medium-priority\",\n" +
			"                \"target\": [\n" +
			"                    {\n" +
			"                        \"measure\": \"จำนวนผู้ป่วยที่ได้รับการยืนยัน\",\n" +
			"                        \"detail\": {\n" +
			"                            \"detailQuantity\": {\n" +
			"                                \"value\": 1.0,\n" +
			"                                \"comparator\": \"&amp;gt;=\",\n" +
			"                                \"unit\": \"case(s)\"\n" +
			"                            }\n" +
			"                        },\n" +
			"                        \"due\": \"2026-12-10\"\n" +
			"                    }\n" +
			"                ]\n" +
			"            },\n" +
			"            {\n" +
			"                \"id\": \"RACD_register_families\",\n" +
			"                \"description\": \"ลงทะเบียนครัวเรือนและสมาชิกในครัวเรือน (100%) ภายในพื้นที่ปฏิบัติงาน\",\n" +
			"                \"priority\": \"medium-priority\",\n" +
			"                \"target\": [\n" +
			"                    {\n" +
			"                        \"measure\": \"ร้อยละของบ้าน สิ่งปลูกสร้างที่ได้ลงทะเบียนข้อมูลครัวเรือน\",\n" +
			"                        \"detail\": {\n" +
			"                            \"detailQuantity\": {\n" +
			"                                \"value\": 100.0,\n" +
			"                                \"comparator\": \"&gt;=\",\n" +
			"                                \"unit\": \"%\"\n" +
			"                            }\n" +
			"                        },\n" +
			"                        \"due\": \"2026-12-10\"\n" +
			"                    }\n" +
			"                ]\n" +
			"            },\n" +
			"            {\n" +
			"                \"id\": \"RACD_Blood_Screening\",\n" +
			"                \"description\": \"เจาะเลือดรอบบ้านผู้ป่วยในรัศมี 1 กิโลเมตร (100%)\",\n" +
			"                \"priority\": \"medium-priority\",\n" +
			"                \"target\": [\n" +
			"                    {\n" +
			"                        \"measure\": \"จำนวนผู้ที่ได้รับการเจาะโลหิต\",\n" +
			"                        \"detail\": {\n" +
			"                            \"detailQuantity\": {\n" +
			"                                \"value\": 50.0,\n" +
			"                                \"comparator\": \"&gt;=\",\n" +
			"                                \"unit\": \"คน\"\n" +
			"                            }\n" +
			"                        },\n" +
			"                        \"due\": \"2026-12-10\"\n" +
			"                    }\n" +
			"                ]\n" +
			"            },\n" +
			"            {\n" +
			"                \"id\": \"BCC_Focus\",\n" +
			"                \"description\": \"ให้สุขศึกษาในพื้นที่ปฏิบัติงานอย่างน้อย 1 ครั้ง\",\n" +
			"                \"priority\": \"medium-priority\",\n" +
			"                \"target\": [\n" +
			"                    {\n" +
			"                        \"measure\": \"จำนวนกิจกรรมการให้สุขศึกษา\",\n" +
			"                        \"detail\": {\n" +
			"                            \"detailQuantity\": {\n" +
			"                                \"value\": 1.0,\n" +
			"                                \"comparator\": \"&gt;=\",\n" +
			"                                \"unit\": \"แห่ง\"\n" +
			"                            }\n" +
			"                        },\n" +
			"                        \"due\": \"2026-12-10\"\n" +
			"                    }\n" +
			"                ]\n" +
			"            },\n" +
			"            {\n" +
			"                \"id\": \"RACD_bednet_distribution\",\n" +
			"                \"description\": \"แจกมุ้งทุกหลังคาเรือนในพื้นที่ปฏิบัติงาน (100%)\",\n" +
			"                \"priority\": \"medium-priority\",\n" +
			"                \"target\": [\n" +
			"                    {\n" +
			"                        \"measure\": \"จำนวนบ้าน/สิ่งปลูกสร้างที่ได้รับมุ้ง\",\n" +
			"                        \"detail\": {\n" +
			"                            \"detailQuantity\": {\n" +
			"                                \"value\": 90.0,\n" +
			"                                \"comparator\": \"&gt;=\",\n" +
			"                                \"unit\": \"%\"\n" +
			"                            }\n" +
			"                        },\n" +
			"                        \"due\": \"2026-12-10\"\n" +
			"                    }\n" +
			"                ]\n" +
			"            },\n" +
			"            {\n" +
			"                \"id\": \"Larval_Dipping\",\n" +
			"                \"description\": \"ดำเนินกิจกรรมจับลูกน้ำอย่างน้อย 3 แห่งในพื้นที่ปฏิบัติงาน\",\n" +
			"                \"priority\": \"medium-priority\",\n" +
			"                \"target\": [\n" +
			"                    {\n" +
			"                        \"measure\": \"จำนวนกิจกรรมการตักลูกน้ำ\",\n" +
			"                        \"detail\": {\n" +
			"                            \"detailQuantity\": {\n" +
			"                                \"value\": 3.0,\n" +
			"                                \"comparator\": \"&gt;=\",\n" +
			"                                \"unit\": \"แห่ง\"\n" +
			"                            }\n" +
			"                        },\n" +
			"                        \"due\": \"2026-12-10\"\n" +
			"                    }\n" +
			"                ]\n" +
			"            },\n" +
			"            {\n" +
			"                \"id\": \"Mosquito_Collection\",\n" +
			"                \"description\": \"กิจกรรมจับยุงกำหนดไว้อย่างน้อย 3 แห่ง\",\n" +
			"                \"priority\": \"medium-priority\",\n" +
			"                \"target\": [\n" +
			"                    {\n" +
			"                        \"measure\": \"จำนวนกิจกรรมการจับยุง\",\n" +
			"                        \"detail\": {\n" +
			"                            \"detailQuantity\": {\n" +
			"                                \"value\": 3.0,\n" +
			"                                \"comparator\": \"&gt;=\",\n" +
			"                                \"unit\": \"แห่ง\"\n" +
			"                            }\n" +
			"                        },\n" +
			"                        \"due\": \"2026-12-10\"\n" +
			"                    }\n" +
			"                ]\n" +
			"            }\n" +
			"        ],\n" +
			"        \"action\": [\n" +
			"            {\n" +
			"                \"identifier\": \"662d4bff-43cc-4f50-af1e-3bc86f8af253\",\n" +
			"                \"prefix\": 1,\n" +
			"                \"title\": \"การยืนยันบ้านผู้ป่วย\",\n" +
			"                \"description\": \"ยืนยันบ้านผู้ป่วย\",\n" +
			"                \"code\": \"Case Confirmation\",\n" +
			"                \"timingPeriod\": {\n" +
			"                    \"start\": \"2021-09-13\",\n" +
			"                    \"end\": \"2021-09-23\"\n" +
			"                },\n" +
			"                \"reason\": \"Investigation\",\n" +
			"                \"goalId\": \"Case_Confirmation\",\n" +
			"                \"subjectCodableConcept\": {\n" +
			"                    \"text\": \"Jurisdiction\"\n" +
			"                },\n" +
			"                \"trigger\": [\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"plan-activation\"\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"condition\": [\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Event is case details event\",\n" +
			"                            \"expression\": \"questionnaire = 'Case_Details'\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"definitionUri\": \"case_confirmation.json\",\n" +
			"                \"type\": \"create\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"identifier\": \"20c71bcf-6aa5-4a14-b360-f4b478919851\",\n" +
			"                \"prefix\": 2,\n" +
			"                \"title\": \"ลงทะเบียนครัวเรือน\",\n" +
			"                \"description\": \"ลงทะเบียนครัวเรือนและสมาชิกในครัวเรือน (100%) ภายในพื้นที่ปฏิบัติงาน\",\n" +
			"                \"code\": \"RACD Register Family\",\n" +
			"                \"timingPeriod\": {\n" +
			"                    \"start\": \"2021-12-10\",\n" +
			"                    \"end\": \"2026-12-10\"\n" +
			"                },\n" +
			"                \"reason\": \"Investigation\",\n" +
			"                \"goalId\": \"RACD_register_families\",\n" +
			"                \"subjectCodableConcept\": {\n" +
			"                    \"text\": \"Location\"\n" +
			"                },\n" +
			"                \"trigger\": [\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"event-submission\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Trigger when a Register_Structure event is submitted\",\n" +
			"                            \"expression\": \"questionnaire = 'Register_Structure' or questionnaire = 'Archive_Family'\"\n" +
			"                        }\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"plan-activation\"\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"condition\": [\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Apply to residential structures in Register_Structure questionnaires\",\n" +
			"                            \"expression\": \"$this.is(FHIR.Location) or (questionnaire = 'Register_Structure' and $this.item.where(linkId='structureType').answer.value ='Residential Structure')\"\n" +
			"                        }\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Structure is residential or type does not exist\",\n" +
			"                            \"expression\": \"$this.is(FHIR.QuestionnaireResponse) or (($this.type.where(id='locationType').exists().not() or $this.type.where(id='locationType').text = 'Residential Structure') and $this.contained.exists().not())\",\n" +
			"                            \"subjectCodableConcept\": {\n" +
			"                                \"text\": \"Family\"\n" +
			"                            }\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"definitionUri\": \"family_register.json\",\n" +
			"                \"type\": \"create\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"identifier\": \"b77fd3d7-80ef-4ea1-aa2d-db70431ab418\",\n" +
			"                \"prefix\": 3,\n" +
			"                \"title\": \"กิจกรรมการเจาะโลหิต\",\n" +
			"                \"description\": \"เจาะเลือดรอบบ้านผู้ป่วยในรัศมี 1 กิโลเมตร (100%)\",\n" +
			"                \"code\": \"Blood Screening\",\n" +
			"                \"timingPeriod\": {\n" +
			"                    \"start\": \"2021-12-10\",\n" +
			"                    \"end\": \"2026-12-10\"\n" +
			"                },\n" +
			"                \"reason\": \"Investigation\",\n" +
			"                \"goalId\": \"RACD_Blood_Screening\",\n" +
			"                \"subjectCodableConcept\": {\n" +
			"                    \"text\": \"Person\"\n" +
			"                },\n" +
			"                \"trigger\": [\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"plan-activation\"\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"event-submission\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Trigger when a Family Registration or Family Member Registration event is submitted\",\n" +
			"                            \"expression\": \"questionnaire = 'Family_Registration' or questionnaire = 'Family_Member_Registration'\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"condition\": [\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Person is older than 5 years or person associated with questionnaire response if older than 5 years\",\n" +
			"                            \"expression\": \"($this.is(FHIR.Patient) and $this.birthDate &lt;= today() - 5 'years') or ($this.contained.where(Patient.birthDate &lt;= today() - 5 'years').exists())\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"definitionUri\": \"blood_screening.json\",\n" +
			"                \"type\": \"create\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"identifier\": \"01851cdb-10e9-4120-9d5e-1921ba7e70a0\",\n" +
			"                \"prefix\": 4,\n" +
			"                \"title\": \"กิจกรรมการให้สุขศึกษา\",\n" +
			"                \"description\": \"ดำเนินกิจกรรมให้สุขศึกษา\",\n" +
			"                \"code\": \"BCC\",\n" +
			"                \"timingPeriod\": {\n" +
			"                    \"start\": \"2021-12-10\",\n" +
			"                    \"end\": \"2026-12-10\"\n" +
			"                },\n" +
			"                \"reason\": \"Investigation\",\n" +
			"                \"goalId\": \"BCC_Focus\",\n" +
			"                \"subjectCodableConcept\": {\n" +
			"                    \"text\": \"Jurisdiction\"\n" +
			"                },\n" +
			"                \"trigger\": [\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"plan-activation\"\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"condition\": [\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Jurisdiction type location\",\n" +
			"                            \"expression\": \"Location.physicalType.coding.exists(code='jdn')\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"definitionUri\": \"behaviour_change_communication.json\",\n" +
			"                \"type\": \"create\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"identifier\": \"82c88bb0-7096-43fe-8c63-942d4b189c07\",\n" +
			"                \"prefix\": 5,\n" +
			"                \"title\": \"กิจกรรมสำรวจ/ชุบ/แจกมุ้ง\",\n" +
			"                \"description\": \"แจกมุ้งทุกหลังคาเรือนในพื้นที่ปฏิบัติงาน (100%)\",\n" +
			"                \"code\": \"Bednet Distribution\",\n" +
			"                \"timingPeriod\": {\n" +
			"                    \"start\": \"2021-12-10\",\n" +
			"                    \"end\": \"2026-12-10\"\n" +
			"                },\n" +
			"                \"reason\": \"Investigation\",\n" +
			"                \"goalId\": \"RACD_bednet_distribution\",\n" +
			"                \"subjectCodableConcept\": {\n" +
			"                    \"text\": \"Location\"\n" +
			"                },\n" +
			"                \"trigger\": [\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"plan-activation\"\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"event-submission\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Trigger when a Family Registration event is submitted\",\n" +
			"                            \"expression\": \"questionnaire = 'Family_Registration'\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"condition\": [\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Structure is residential or type does not exist\",\n" +
			"                            \"expression\": \"$this.is(FHIR.QuestionnaireResponse) or (($this.type.where(id='locationType').exists().not() or $this.type.where(id='locationType').text = 'Residential Structure') and $this.contained.exists())\",\n" +
			"                            \"subjectCodableConcept\": {\n" +
			"                                \"text\": \"Family\"\n" +
			"                            }\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"definitionUri\": \"bednet_distribution.json\",\n" +
			"                \"type\": \"create\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"identifier\": \"360e6f65-ae96-44e7-9728-30894d7bd6d3\",\n" +
			"                \"prefix\": 6,\n" +
			"                \"title\": \"กิจกรรมการตักลูกน้ำ\",\n" +
			"                \"description\": \"ดำเนินกิจกรรมจับลูกน้ำอย่างน้อย 3 แห่งในพื้นที่ปฏิบัติงาน\",\n" +
			"                \"code\": \"Larval Dipping\",\n" +
			"                \"timingPeriod\": {\n" +
			"                    \"start\": \"2021-12-10\",\n" +
			"                    \"end\": \"2026-12-10\"\n" +
			"                },\n" +
			"                \"reason\": \"Investigation\",\n" +
			"                \"goalId\": \"Larval_Dipping\",\n" +
			"                \"subjectCodableConcept\": {\n" +
			"                    \"text\": \"Location\"\n" +
			"                },\n" +
			"                \"trigger\": [\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"event-submission\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Trigger when a Register_Structure event is submitted\",\n" +
			"                            \"expression\": \"questionnaire = 'Register_Structure'\"\n" +
			"                        }\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"plan-activation\"\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"condition\": [\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Apply to larval breeding sites in Register_Structure questionnaires\",\n" +
			"                            \"expression\": \"$this.is(FHIR.Location) or (questionnaire = 'Register_Structure' and $this.item.where(linkId='structureType').answer.value ='Larval Breeding Site')\"\n" +
			"                        }\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Structure is a larval breeding site\",\n" +
			"                            \"expression\": \"$this.is(FHIR.QuestionnaireResponse) or $this.type.where(id='locationType').text = 'Larval Breeding Site'\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"definitionUri\": \"larval_dipping_form.json\",\n" +
			"                \"type\": \"create\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"identifier\": \"5b3b8182-8745-400b-bfe4-12abee1fb086\",\n" +
			"                \"prefix\": 7,\n" +
			"                \"title\": \"กิจกรรมการจับยุง\",\n" +
			"                \"description\": \"กิจกรรมจับยุงกำหนดไว้อย่างน้อย 3 แห่ง\",\n" +
			"                \"code\": \"Mosquito Collection\",\n" +
			"                \"timingPeriod\": {\n" +
			"                    \"start\": \"2021-12-10\",\n" +
			"                    \"end\": \"2026-12-10\"\n" +
			"                },\n" +
			"                \"reason\": \"Investigation\",\n" +
			"                \"goalId\": \"Mosquito_Collection\",\n" +
			"                \"subjectCodableConcept\": {\n" +
			"                    \"text\": \"Location\"\n" +
			"                },\n" +
			"                \"trigger\": [\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"plan-activation\"\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"type\": \"named-event\",\n" +
			"                        \"name\": \"event-submission\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Trigger when a Register_Structure event is submitted\",\n" +
			"                            \"expression\": \"questionnaire = 'Register_Structure'\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"condition\": [\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Apply to mosquito collection point in Register_Structure questionnaires\",\n" +
			"                            \"expression\": \"$this.is(FHIR.Location) or (questionnaire = 'Register_Structure' and $this.item.where(linkId='structureType').answer.value ='Mosquito Collection Point')\"\n" +
			"                        }\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"kind\": \"applicability\",\n" +
			"                        \"expression\": {\n" +
			"                            \"description\": \"Structure is a mosquito collection point\",\n" +
			"                            \"expression\": \"$this.is(FHIR.QuestionnaireResponse) or $this.type.where(id='locationType').text = 'Mosquito Collection Point'\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"definitionUri\": \"mosquito_collection_form.json\",\n" +
			"                \"type\": \"create\"\n" +
			"            }\n" +
			"        ],\n" +
			"        \"experimental\": false\n" +
			"    }";

	private ArgumentCaptor<PlanDefinition> argumentCaptor = ArgumentCaptor.forClass(PlanDefinition.class);
	
	private Class<ArrayList<String>> listClass = (Class<ArrayList<String>>) (Class) ArrayList.class;
	
	@Captor
	private ArgumentCaptor<ArrayList<String>> listArgumentCaptor = ArgumentCaptor.forClass(listClass);
	
	@Captor
	private ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
	
	@Captor
	private ArgumentCaptor<List<Long>> orgsArgumentCaptor;

	@Captor
	private ArgumentCaptor<String> stringArgumentCaptor;

	@Captor
	private ArgumentCaptor<Boolean> booleanArgumentCaptor = ArgumentCaptor.forClass(boolean.class);

	private PlanResource planResource;
	private TemplateService templateService;
	private EventService eventService;
	private PlanProcessingStatusService processingStatusService;

	@Before
	public void setUp() {
		planService = mock(PlanService.class);
		locationService = mock(PhysicalLocationService.class);
		templateService = mock(TemplateService.class);
		eventService = mock(EventService.class);
		processingStatusService = mock(PlanProcessingStatusService.class);
		planResource = webApplicationContext.getBean(PlanResource.class);
		planResource.setPlanService(planService);
		planResource.setLocationService(locationService);
		planResource.setTemplateService(templateService);
		planResource.setEventService(eventService);
		planResource.setProcessingStatusService(processingStatusService);
	}
	
	@Test
	public void testGetPlansShouldReturnAllPlans() throws Exception {
		List<PlanDefinition> expectedPlans = new ArrayList<>();
		
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		
		expectedPlans.add(expectedPlan);
		
		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_2");
		expectedPlan.setJurisdiction(operationalAreas);
		
		expectedPlans.add(expectedPlan);

		doReturn(expectedPlans).when(planService).getAllPlans(any(PlanSearchBean.class));
		
		String actualPlansString = getResponseAsString(BASE_URL, null, status().isOk());
		List<PlanDefinition> actualPlans = new Gson().fromJson(actualPlansString,
		    new TypeToken<List<PlanDefinition>>() {}.getType());
		
		assertListsAreSameIgnoringOrder(actualPlans, expectedPlans);
	}
	
	@Test
	public void testGetPlanByUniqueIdShouldReturnCorrectPlan() throws Exception {
		List<PlanDefinition> expectedPlans = new ArrayList<>();
		
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		
		List<String> planIdList = new ArrayList<>();
		planIdList.add(expectedPlan.getIdentifier());
		expectedPlans.add(expectedPlan);

		doReturn(expectedPlans).when(planService).getPlansByIdsReturnOptionalFields(anyList(),
		    eq(null), anyBoolean());
		
		String actualPlansString = getResponseAsString(BASE_URL + "plan_1", null, status().isOk());
		List<PlanDefinition> actualPlanList = new Gson().fromJson(actualPlansString,
		    new TypeToken<List<PlanDefinition>>() {}.getType());
		
		assertNotNull(actualPlanList);
		assertEquals(1, actualPlanList.size());
		PlanDefinition actualPlan = actualPlanList.get(0);
		
		assertEquals(actualPlan.getIdentifier(), expectedPlan.getIdentifier());
		assertEquals(actualPlan.getJurisdiction().get(0).getCode(), expectedPlan.getJurisdiction().get(0).getCode());
	}
	
	@Test
	public void testCreateShouldCreateNewPlanResource() throws Exception {
		doReturn(new PlanDefinition()).when(planService).addPlan(any(PlanDefinition.class), anyString());
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area_1");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		
		postRequestWithJsonContent(BASE_URL, plansJson, status().isCreated());
		
		verify(planService).addPlan(argumentCaptor.capture(), eq(authenticatedUser.getFirst().getUsername()));
		assertEquals(argumentCaptor.getValue().getIdentifier(), expectedPlan.getIdentifier());
	}
	
	@Test
	public void testCreateShouldThrowException() throws Exception {
		doThrow(new JsonSyntaxException("Unable to parse exception")).when(planService).addPlan(any(PlanDefinition.class),
		    anyString());
		postRequestWithJsonContent(BASE_URL, plansJson, status().isBadRequest());
	}
	
	@Test
	public void testUpdateShouldUpdateExistingPlanResource() throws Exception {
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area_1");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		
		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area_2");
		operationalAreas.clear();
		operationalAreas.add(operationalArea);
		expectedPlan.setJurisdiction(operationalAreas);
		
		String plansJson = new Gson().toJson(expectedPlan, new TypeToken<PlanDefinition>() {}.getType());
		putRequestWithJsonContent(BASE_URL, plansJson, status().isCreated());
		
		verify(planService).updatePlan(argumentCaptor.capture(), eq(authenticatedUser.getFirst().getUsername()), eq(true));
		assertEquals(argumentCaptor.getValue().getIdentifier(), expectedPlan.getIdentifier());
	}

	@Test
	public void testUpdateShouldUpdateExistingPlanResourceWithRevokeAssignment() throws Exception {
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area_1");
		operationalAreas.add(operationalArea);

		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);

		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area_2");
		operationalAreas.clear();
		operationalAreas.add(operationalArea);
		expectedPlan.setJurisdiction(operationalAreas);

		String plansJson = new Gson().toJson(expectedPlan, new TypeToken<PlanDefinition>() {}.getType());

		putRequestWithJsonContent(BASE_URL + "?revoke_assignments=false", plansJson, status().isCreated());
		verify(planService).updatePlan(argumentCaptor.capture(), eq(authenticatedUser.getFirst().getUsername()), eq(false));
		assertEquals(argumentCaptor.getValue().getIdentifier(), expectedPlan.getIdentifier());
	}
	
	@Test
	public void testUpdateShouldThrowException() throws Exception {
		doThrow(new JsonSyntaxException("Unable to parse exception"))
				.when(planService)
				.updatePlan(any(PlanDefinition.class), anyString(), anyBoolean());
		putRequestWithJsonContent(BASE_URL, plansJson, status().isBadRequest());
	}
	
	@Test
	public void testSyncByServerVersionAndAssignedPlansOnOrganization() throws Exception {
		List<PlanDefinition> expectedPlans = new ArrayList<>();
		
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(1l);
		expectedPlans.add(expectedPlan);
		
		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_2");
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(0l);
		expectedPlans.add(expectedPlan);
		
		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_3");
		operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area_2");
		operationalAreas.clear();
		operationalAreas.add(operationalArea);
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(1l);
		expectedPlans.add(expectedPlan);
		
		doReturn(expectedPlans).when(planService).getPlansByOrganizationsAndServerVersion(anyList(), anyLong(), anyBoolean());
		
		String data = "{\"serverVersion\":\"1\",\"operational_area_id\":[\"operational_area\",\"operational_area_2\"],\"organizations\":[2]}";
		String actualPlansString = postRequestWithJsonContentAndReturnString(BASE_URL + "sync", data, status().isOk());
		
		Gson gson = PlanResource.gson;
		List<PlanDefinition> actualPlans = gson.fromJson(actualPlansString,
		    new TypeToken<List<PlanDefinition>>() {}.getType());
		
		verify(planService).getPlansByOrganizationsAndServerVersion(orgsArgumentCaptor.capture(),
		    longArgumentCaptor.capture(), booleanArgumentCaptor.capture());
		assertEquals(longArgumentCaptor.getValue().longValue(), 1);
		List<Long> list = orgsArgumentCaptor.getValue();
		assertEquals(2l, list.get(0), 0);
		assertEquals(1, longArgumentCaptor.getValue(), 0);
		
		assertEquals(3, actualPlans.size());
		assertEquals("plan_1", actualPlans.get(0).getIdentifier());
		assertEquals("plan_2", actualPlans.get(1).getIdentifier());
		assertEquals("plan_3", actualPlans.get(2).getIdentifier());
		
		assertEquals(gson.toJson(expectedPlans), gson.toJson(actualPlans));
		
	}
	
	@Test
	public void testSyncByServerVersionAndAssignedPlansOnOrganizationWithoutOrgsAndAuthencation() throws Exception {
		List<PlanDefinition> expectedPlans = new ArrayList<>();
		
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(1l);
		expectedPlans.add(expectedPlan);
		
		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_2");
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(0l);
		expectedPlans.add(expectedPlan);
		
		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_3");
		operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area_2");
		operationalAreas.clear();
		operationalAreas.add(operationalArea);
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(1l);
		expectedPlans.add(expectedPlan);
		
		doReturn(expectedPlans).when(planService).getPlansByOrganizationsAndServerVersion(anyList(), anyLong(), anyBoolean());
		
		String data = "{\"serverVersion\":\"1\",\"operational_area_id\":[\"operational_area\",\"operational_area_2\"]}";
		
		this.mockMvc
		        .perform(post(BASE_URL + "sync").contentType(MediaType.APPLICATION_JSON).content(data.getBytes())
		                .accept(MediaType.APPLICATION_JSON))
		        .andExpect(status().isBadRequest()).andReturn();
		
	
		verify(planService, never()).getPlansByOrganizationsAndServerVersion(orgsArgumentCaptor.capture(),
		    longArgumentCaptor.capture(), booleanArgumentCaptor.capture());
		
	}
	
	public void testGetSyncByServerVersionAndOperationalAreaShouldSyncCorrectPlans() throws Exception {
		List<PlanDefinition> expectedPlans = new ArrayList<>();
		
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(1l);
		expectedPlans.add(expectedPlan);
		
		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_2");
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(0l);
		
		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_3");
		operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area_2");
		operationalAreas.clear();
		operationalAreas.add(operationalArea);
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(1l);
		expectedPlans.add(expectedPlan);
		
		doReturn(expectedPlans).when(planService).getPlansByServerVersionAndOperationalArea(anyLong(), anyList(), anyBoolean());
		
		String actualPlansString = getResponseAsString(BASE_URL + "sync", SERVER_VERSIOIN + "=" + 1 + "&"
		        + OPERATIONAL_AREA_ID + "=" + "operational_area" + "&" + OPERATIONAL_AREA_ID + "=" + "operational_area_2",
		    status().isOk());
		List<PlanDefinition> actualPlans = new Gson().fromJson(actualPlansString,
		    new TypeToken<List<PlanDefinition>>() {}.getType());
		
		verify(planService).getPlansByServerVersionAndOperationalArea(longArgumentCaptor.capture(),
		    listArgumentCaptor.capture(), booleanArgumentCaptor.capture());
		assertEquals(longArgumentCaptor.getValue().longValue(), 1);
		assertEquals(listArgumentCaptor.getValue().get(0), "operational_area");
		assertEquals(expectedPlans, actualPlans);
	}
	
	@Override
	protected void assertListsAreSameIgnoringOrder(List<PlanDefinition> expectedList, List<PlanDefinition> actualList) {
		if (expectedList == null || actualList == null) {
			throw new AssertionError("One of the lists is null");
		}
		
		assertEquals(expectedList.size(), actualList.size());
		
		Set<String> expectedIds = new HashSet<>();
		for (PlanDefinition plan : expectedList) {
			expectedIds.add(plan.getIdentifier());
		}
		
		for (PlanDefinition plan : actualList) {
			assertTrue(expectedIds.contains(plan.getIdentifier()));
		}
	}
	
	@Test
	public void testfindByIdentifiersReturnOptionalFieldsShouldReturnCorrectPlans() throws Exception {
		
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		
		List<String> planIdList = new ArrayList<>();
		planIdList.add(expectedPlan.getIdentifier());
		
		List<String> fieldNameList = new ArrayList<>();
		fieldNameList.add("action");
		fieldNameList.add("name");
		
		doReturn(Collections.singletonList(expectedPlan)).when(planService).getPlansByIdsReturnOptionalFields(planIdList,
		    fieldNameList, false);
		
		String actualPlansString = getResponseAsString(
		    BASE_URL + "findByIdsWithOptionalFields?identifiers=" + expectedPlan.getIdentifier() + "&fields=action,name",
		    null, status().isOk());
		List<PlanDefinition> actualPlanList = new Gson().fromJson(actualPlansString,
		    new TypeToken<List<PlanDefinition>>() {}.getType());
		
		assertNotNull(actualPlanList);
		assertEquals(1, actualPlanList.size());
		PlanDefinition actualPlan = actualPlanList.get(0);
		
		assertEquals(actualPlan.getIdentifier(), expectedPlan.getIdentifier());
		assertEquals(actualPlan.getJurisdiction().get(0).getCode(), expectedPlan.getJurisdiction().get(0).getCode());
	}
	
	@Test
	public void testFindLocationNamesByPlanId() throws Exception {
		LocationDetail locationDetail = new LocationDetail();
		locationDetail.setIdentifier("304cbcd4-0850-404a-a8b1-486b02f7b84d");
		locationDetail.setName("location one");
		
		Set<LocationDetail> locationDetails = Collections.singleton(locationDetail);
		when(locationService.findLocationDetailsByPlanId(anyString())).thenReturn(locationDetails);
		MvcResult result = mockMvc.perform(get(BASE_URL + "findLocationNames/{planIdentifier}", "plan_id"))
		        .andExpect(status().isOk()).andReturn();
		verify(locationService).findLocationDetailsByPlanId(anyString());
		assertEquals(new GsonBuilder().serializeNulls().create().toJson(locationDetails),
		    result.getResponse().getContentAsString());
		
	}
	
	@Test
	public void testGetAll() throws Exception {
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		
		List<PlanDefinition> planDefinitions = Collections.singletonList(expectedPlan);
		when(planService.getAllPlans(anyLong(), anyInt(), anyBoolean())).thenReturn(planDefinitions);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/getAll?serverVersion=0&limit=25")).andExpect(status().isOk())
		        .andReturn();
		verify(planService).getAllPlans(anyLong(), anyInt(), anyBoolean());
		assertEquals(PlanResource.gson.toJson(planDefinitions), result.getResponse().getContentAsString());
		
	}

	@Test
	public void testCountAll() throws Exception {
		when(planService.countAllPlans(anyLong(), anyBoolean())).thenReturn(1l);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/countAll?serverVersion=0")).andExpect(status().isOk())
				.andReturn();
		verify(planService).countAllPlans(anyLong(), anyBoolean());
		assertEquals(1 , new JSONObject(result.getResponse().getContentAsString()).optInt("count"));
	}
	
	@Test
	public void testFindAllIds() throws Exception {
		Pair<List<String>, Long> idsModel = Pair.of(Collections.singletonList("plan-id-1"), 12345l);
		when(planService.findAllIds(anyLong(), anyInt(), anyBoolean(), isNull(), isNull())).thenReturn(idsModel);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/findIds?serverVersion=0", "")).andExpect(status().isOk())
		        .andReturn();
		
		String actualTaskIdString = result.getResponse().getContentAsString();
		Identifier actualIdModels = new Gson().fromJson(actualTaskIdString, new TypeToken<Identifier>() {}.getType());
		List<String> actualTaskIdList = actualIdModels.getIdentifiers();
		
		verify(planService).findAllIds(anyLong(), anyInt(), anyBoolean(), isNull(), isNull());
		verifyNoMoreInteractions(planService);
		assertEquals("{\"identifiers\":[\"plan-id-1\"],\"lastServerVersion\":12345}",
		    result.getResponse().getContentAsString());
		assertEquals((idsModel.getLeft()).get(0), actualTaskIdList.get(0));
		assertEquals(idsModel.getRight(), actualIdModels.getLastServerVersion());
	}
	
	@Test
	public void testFindByUsername() throws Exception {
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		
		List<PlanDefinition> planDefinitions = Collections.singletonList(expectedPlan);
		when(planService.getPlansByUsernameAndServerVersion("onatest", 0l, false)).thenReturn(planDefinitions);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/user/onatest?serverVersion=0")).andExpect(status().isOk())
		        .andReturn();
		verify(planService).getPlansByUsernameAndServerVersion("onatest", 0l, false);
		assertEquals(PlanResource.gson.toJson(planDefinitions), result.getResponse().getContentAsString());
		
	}
	
	@Test
	public void testGetSync() throws Exception {
		List<PlanDefinition> planDefinitions = new ArrayList<>();
		planDefinitions.add(createPlanDefinition());
		List<String> operationalAreaIds = new ArrayList<>();
		operationalAreaIds.add("1");
		
		doReturn(planDefinitions).when(planService).getPlansByServerVersionAndOperationalArea(any(long.class), anyList(), anyBoolean());
		
		String parameter = SERVER_VERSIOIN + "=15421904649873&" + OPERATIONAL_AREA_ID + "=[1]";
		String response = getResponseAsString(BASE_URL + "/sync", parameter, status().isOk());
		JsonNode actualObj = mapper.readTree(response);
		
		verify(planService).getPlansByServerVersionAndOperationalArea(longArgumentCaptor.capture(),
		    listArgumentCaptor.capture(), booleanArgumentCaptor.capture());
		assertEquals(longArgumentCaptor.getValue().longValue(), 15421904649873l);
		assertEquals(listArgumentCaptor.getAllValues().size(), operationalAreaIds.size());
		assertEquals(actualObj.get(0).get("identifier").textValue(), planDefinitions.get(0).getIdentifier());
		
	}
	
	@Test
	public void testSyncByServerVersionAndAssignedPlansOnOrganizationWithReturnCount() throws Exception {
		List<PlanDefinition> expectedPlans = new ArrayList<>();
		long returnCount = 9l;

		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);

		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(1l);
		expectedPlans.add(expectedPlan);

		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_2");
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(0l);
		expectedPlans.add(expectedPlan);

		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_3");
		operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area_2");
		operationalAreas.clear();
		operationalAreas.add(operationalArea);
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(1l);
		expectedPlans.add(expectedPlan);

		doReturn(expectedPlans).when(planService).getPlansByOrganizationsAndServerVersion(anyList(), anyLong(), anyBoolean());
		doReturn(returnCount).when(planService).countPlansByOrganizationsAndServerVersion(anyList(), anyLong());

		String data = "{\"serverVersion\":\"1\",\"operational_area_id\":[\"operational_area\",\"operational_area_2\"],\"organizations\":[2], \"return_count\":true}";
		String actualPlansString = postRequestWithJsonContentAndReturnString(BASE_URL + "sync", data, status().isOk());

		Gson gson =PlanResource.gson;
		List<PlanDefinition> actualPlans = gson.fromJson(actualPlansString, new TypeToken<List<PlanDefinition>>(){}.getType());

		verify(planService).getPlansByOrganizationsAndServerVersion(orgsArgumentCaptor.capture(), longArgumentCaptor.capture(), booleanArgumentCaptor.capture());
		verify(planService).countPlansByOrganizationsAndServerVersion(any(), anyLong());
		assertEquals(longArgumentCaptor.getValue().longValue(), 1);
		List<Long> list  = orgsArgumentCaptor.getValue();
		assertEquals(2l,list.get(0),0);
		assertEquals(1,longArgumentCaptor.getValue(),0);

		assertEquals(3, actualPlans.size());
		assertEquals("plan_1", actualPlans.get(0).getIdentifier());
		assertEquals("plan_2", actualPlans.get(1).getIdentifier());
		assertEquals("plan_3", actualPlans.get(2).getIdentifier());
		assertEquals(gson.toJson(expectedPlans), gson.toJson(actualPlans));

	}
	
	@Test
	public void testCreateCaseTriggeredPlanShouldReturnErrorForPlanWithExistingOpensrpEventId() throws Exception {
		doReturn(new PlanDefinition()).when(planService).addPlan(any(PlanDefinition.class), anyString());
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area_1");
		operationalAreas.add(operationalArea);

		List<PlanDefinition> existingPlans = new ArrayList<>();
		PlanDefinition existingPlan = new PlanDefinition();
		existingPlan.setIdentifier("plan_1");
		existingPlan.setJurisdiction(operationalAreas);

		PlanDefinition.UseContext opensrpEventIdUseContext = new PlanDefinition.UseContext();
		opensrpEventIdUseContext.setCode("opensrpEventId");
		opensrpEventIdUseContext.setValueCodableConcept("opensrp_event_id_1");
		existingPlan.setUseContext(new ArrayList<>());
		existingPlan.getUseContext().add(opensrpEventIdUseContext);
		existingPlans.add(existingPlan);

		PlanDefinition casetriggeredPlan = new PlanDefinition();
		casetriggeredPlan.setIdentifier("plan_1");
		casetriggeredPlan.setJurisdiction(operationalAreas);

		casetriggeredPlan = new PlanDefinition();
		casetriggeredPlan.setIdentifier("plan_1");
		operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area_2");
		operationalAreas.clear();
		operationalAreas.add(operationalArea);
		casetriggeredPlan.setJurisdiction(operationalAreas);
		casetriggeredPlan.setUseContext(new ArrayList<>());
		casetriggeredPlan.getUseContext().add(opensrpEventIdUseContext);

		String plansJson = new Gson().toJson(casetriggeredPlan, new TypeToken<PlanDefinition>() {}.getType());

		doReturn(existingPlans).when(planService).getAllPlans(any(PlanSearchBean.class));

		String reponseString = postRequestWithJsonContentAndReturnString(BASE_URL, plansJson, status().isConflict());

		verify(planService, never()).addPlan(argumentCaptor.capture(), eq(authenticatedUser.getFirst().getUsername()));
		assertEquals("Case triggered plan with opensrpEventId opensrp_event_id_1 already exists", reponseString);
	}

	@Test
	public void testFindMissingTaskGeneration() throws Exception {
		PlanTaskCount planTaskCount = new PlanTaskCount();
		TaskCount taskCount = new TaskCount();
		taskCount.setCode(PlanConstants.CASE_CONFIRMATION);
		taskCount.setExpectedCount(3l);
		taskCount.setActualCount(2l);
		taskCount.setMissingCount(1l);
		planTaskCount.setTaskCounts(Collections.singletonList(taskCount));
		planTaskCount.setPlanIdentifier("identifier-1");
		doReturn(Collections.singletonList(planTaskCount)).when(planService).getPlanTaskCounts(any(),any(),any());
		MvcResult result = mockMvc.perform(get(BASE_URL + "/findMissingTaskGeneration")).andExpect(status().isOk())
				.andReturn();
		verify(planService).getPlanTaskCounts(any(),any(),any());
		assertEquals("[{\"planIdentifier\":\"identifier-1\",\"taskCounts\":[{\"code\":\"Case Confirmation\",\"actualCount\":2,\"expectedCount\":3,\"missingCount\":1}]}]", result.getResponse().getContentAsString());
	}

	@Test
	public void testGenerateCaseTriggeredPlans() {
		PlanDefinition expectedPlan = gson.fromJson(caseTriggeredPlanJson, PlanDefinition.class);
		PlanProcessingStatus status = new PlanProcessingStatus();
		status.setStatus(0);
		status.setTemplateId(1l);
		status.setEventId(1l);
		when(processingStatusService.getProcessingStatusByStatus(0))
				.thenReturn(Collections.singletonList(status));

		Event event = gson.fromJson(caseDetailsEventJson, Event.class);
		when(eventService.findByDbId(status.getEventId(),false)).thenReturn(event);

		when(planService.validateCaseDetailsEvent(event)).thenReturn(true);

		Map<String, String> properties = new HashMap<>();
		properties.put(Constants.Plan.EXTERNAL_ID, event.getDetails().get(Constants.Plan.BFID));
		PhysicalLocation location = new PhysicalLocation();
		location.setId("location-id");
		when(locationService.findLocationsByProperties(false, null, properties))
				.thenReturn(Collections.singletonList(location));

		when(planService.getPlanTemplate(event)).thenReturn(1);

		Template template = gson.fromJson(templateString, Template.class);
		when(templateService.getTemplateByTemplateId(1)).thenReturn(template);

	    PlanResource planResourceSpy = spy(planResource);
		Date todayDate = new DateTime(2021, 12, 10, 0, 0, 0, 0).toDate();
		when(planResourceSpy.getCurrentDate()).thenReturn(todayDate);

		planResourceSpy.generateCaseTriggeredPlans();

		verify(planService).addPlan(argumentCaptor.capture(), stringArgumentCaptor.capture());
		assertEquals(Constants.Plan.PLAN_USER, stringArgumentCaptor.getValue());
		PlanDefinition actualPlan = argumentCaptor.getValue();
		assertEquals(expectedPlan.getIdentifier(), actualPlan.getIdentifier());
		assertEquals(expectedPlan.getName(), actualPlan.getName());
		assertEquals(expectedPlan.getTitle(), actualPlan.getTitle());
		verify(processingStatusService).updatePlanProcessingStatus(status, null,
				actualPlan.getIdentifier(), PlanProcessingStatusConstants.COMPLETE, null);

	}

	@Test
	public void testGenerateCaseTriggeredPlansLogsErrorWhenEventIsInvalid() {
		PlanProcessingStatus status = new PlanProcessingStatus();
		status.setStatus(0);
		status.setTemplateId(1l);
		status.setEventId(1l);
		when(processingStatusService.getProcessingStatusByStatus(0))
				.thenReturn(Collections.singletonList(status));

		when(eventService.findByDbId(status.getEventId(),false)).thenReturn(null);

		PlanResource planResourceSpy = spy(planResource);
		planResourceSpy.generateCaseTriggeredPlans();

		verify(processingStatusService).updatePlanProcessingStatus(status, null,
				null, PlanProcessingStatusConstants.FAILED, "Case details event does not exist");

		verify(planService,times(0)).addPlan(any(),any());

	}

	@Test
	public void testGenerateCaseTriggeredPlansLogsErrorWhenEventIsMissing() {
		PlanProcessingStatus status = new PlanProcessingStatus();
		status.setStatus(0);
		status.setTemplateId(1l);
		status.setEventId(1l);
		when(processingStatusService.getProcessingStatusByStatus(0))
				.thenReturn(Collections.singletonList(status));

		Event event = gson.fromJson(caseDetailsEventJson, Event.class);
		when(eventService.findByDbId(status.getEventId(),false)).thenReturn(event);

		when(planService.validateCaseDetailsEvent(event)).thenReturn(false);

		PlanResource planResourceSpy = spy(planResource);
		planResourceSpy.generateCaseTriggeredPlans();

		verify(processingStatusService).updatePlanProcessingStatus(status, null,
				null, PlanProcessingStatusConstants.FAILED, "Case details event is invalid");

		verify(planService,times(0)).addPlan(any(),any());

	}

	@Test
	public void testGenerateCaseTriggeredPlansLogsErrorWhenBiophicsLocationIdIsMissing() {
		PlanProcessingStatus status = new PlanProcessingStatus();
		status.setStatus(0);
		status.setTemplateId(1l);
		status.setEventId(1l);
		when(processingStatusService.getProcessingStatusByStatus(0))
				.thenReturn(Collections.singletonList(status));

		Event event = gson.fromJson(caseDetailsEventJson, Event.class);
		event.getDetails().remove(Constants.Plan.BFID);
		when(eventService.findByDbId(status.getEventId(),false)).thenReturn(event);

		when(planService.validateCaseDetailsEvent(event)).thenReturn(true);

		PlanResource planResourceSpy = spy(planResource);
		planResourceSpy.generateCaseTriggeredPlans();

		verify(processingStatusService).updatePlanProcessingStatus(status, null,
				null, PlanProcessingStatusConstants.FAILED, "Biophics id missing from case details event");

		verify(planService,times(0)).addPlan(any(),any());

	}

	@Test
	public void testGenerateCaseTriggeredPlansLogsErrorWhenOpensrpLocationIsMissing() {
		PlanProcessingStatus status = new PlanProcessingStatus();
		status.setStatus(0);
		status.setTemplateId(1l);
		status.setEventId(1l);
		when(processingStatusService.getProcessingStatusByStatus(0))
				.thenReturn(Collections.singletonList(status));

		Event event = gson.fromJson(caseDetailsEventJson, Event.class);
		when(eventService.findByDbId(status.getEventId(),false)).thenReturn(event);

		when(planService.validateCaseDetailsEvent(event)).thenReturn(true);

		Map<String, String> properties = new HashMap<>();
		properties.put(Constants.Plan.EXTERNAL_ID, event.getDetails().get(Constants.Plan.BFID));
		PhysicalLocation location = new PhysicalLocation();
		location.setId("location-id");
		when(locationService.findLocationsByProperties(false, null, properties))
				.thenReturn(null);

		PlanResource planResourceSpy = spy(planResource);
		planResourceSpy.generateCaseTriggeredPlans();

		verify(processingStatusService).updatePlanProcessingStatus(status, null,
				null, PlanProcessingStatusConstants.FAILED, "Jurisdiction not found");

		verify(planService,times(0)).addPlan(any(),any());

	}

	@Test
	public void testGenerateCaseTriggeredPlansLogsErrorWhenTemplateIsMissing() {
		PlanProcessingStatus status = new PlanProcessingStatus();
		status.setStatus(0);
		status.setTemplateId(1l);
		status.setEventId(1l);
		when(processingStatusService.getProcessingStatusByStatus(0))
				.thenReturn(Collections.singletonList(status));

		Event event = gson.fromJson(caseDetailsEventJson, Event.class);
		when(eventService.findByDbId(status.getEventId(),false)).thenReturn(event);

		when(planService.validateCaseDetailsEvent(event)).thenReturn(true);

		Map<String, String> properties = new HashMap<>();
		properties.put(Constants.Plan.EXTERNAL_ID, event.getDetails().get(Constants.Plan.BFID));
		PhysicalLocation location = new PhysicalLocation();
		location.setId("location-id");
		when(locationService.findLocationsByProperties(false, null, properties))
				.thenReturn(Collections.singletonList(location));

		when(planService.getPlanTemplate(event)).thenReturn(null);

		PlanResource planResourceSpy = spy(planResource);
		planResourceSpy.generateCaseTriggeredPlans();

		verify(processingStatusService).updatePlanProcessingStatus(status, null,
				null, PlanProcessingStatusConstants.FAILED, "Plan template not found");

		verify(planService,times(0)).addPlan(any(),any());

	}


	private PlanDefinition createPlanDefinition() {
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		return expectedPlan;
	}
	
}
