package org.opensrp;

public class TestFileContent {

    public static final String CALCULATION_YAML_FILE_CONTENT = "---\n" +
            "name: step1_danger_signs_toaster\n" +
            "description: Displays the womans danger signs.\n" +
            "priority: 1\n" +
            "condition: \"!global_danger_signs.isEmpty() && !global_danger_signs.contains('danger_none')\"\n" +
            "actions:\n" +
            " - 'calculation = [\"danger_signs\" : global_danger_signs_value]'\n" +
            "---\n" +
            "name: step1_severe_hypertension_toaster\n" +
            "description: Severe hypertension toaster\n" +
            "priority: 1\n" +
            "condition: \"global_severe_hypertension == 1\"\n" +
            "actions:\n" +
            " - 'calculation = [\"bp_systolic_repeat\" : global_bp_systolic_repeat, \"bp_diastolic_repeat\" : global_bp_diastolic_repeat]'\n" +
            "---\n" +
            "name: step1_hypertension_pre_eclampsia_toaster\n" +
            "description: Hypertension and symptom of severe\n" +
            "priority: 1\n" +
            "condition: \"global_hypertension == 1 && !global_symp_sev_preeclampsia.isEmpty() && !global_symp_sev_preeclampsia.contains('none')\"\n" +
            "actions:\n" +
            "  - 'calculation = [\"symp_sev_preeclampsia\" : global_symp_sev_preeclampsia_value]'";
    public static final String JMAG_PROPERTIES_FILE_CONTENT = "anc_register.step1.dob_unknown.options.dob_unknown.text = DOB unknown?\n" +
            "anc_register.step1.reminders.options.no.text = No\n" +
            "anc_register.step1.reminders.label = Woman wants to receive reminders during pregnancy\n" +
            "anc_register.step1.dob_entered.v_required.err = Please enter the date of birth\n" +
            "anc_register.step1.home_address.hint = Home address\n" +
            "anc_register.step1.alt_name.v_regex.err = Please enter a valid VHT name\n" +
            "anc_register.step1.anc_id.hint = ANC ID\n" +
            "anc_register.step1.alt_phone_number.v_numeric.err = Phone number must be numeric\n" +
            "anc_register.step1.age_entered.v_numeric.err = Age must be a number\n" +
            "anc_register.step1.phone_number.hint = Mobile phone number\n" +
            "anc_register.step1.last_name.v_required.err = Please enter the last name\n" +
            "anc_register.step1.last_name.v_regex.err = Please enter a valid name\n" +
            "anc_register.step1.first_name.v_required.err = Please enter the first name\n" +
            "anc_register.step1.dob_entered.hint = Date of birth (DOB)\n" +
            "anc_register.step1.age_entered.hint = Age\n" +
            "anc_register.step1.reminders.label_info_text = Does she want to receive reminders for care and messages regarding her health throughout her pregnancy?\n" +
            "anc_register.step1.title = ANC Registration\n" +
            "anc_register.step1.anc_id.v_numeric.err = Please enter a valid ANC ID\n" +
            "anc_register.step1.alt_name.hint = Alternate contact name\n" +
            "anc_register.step1.home_address.v_required.err = Please enter the woman's home address\n" +
            "anc_register.step1.first_name.hint = First name\n" +
            "anc_register.step1.alt_phone_number.hint = Alternate contact phone number\n" +
            "anc_register.step1.reminders.v_required.err = Please select whether the woman has agreed to receiving reminder notifications\n" +
            "anc_register.step1.first_name.v_regex.err = Please enter a valid name\n" +
            "anc_register.step1.reminders.options.yes.text = Yes\n" +
            "anc_register.step1.phone_number.v_numeric.err = Phone number must be numeric\n" +
            "anc_register.step1.anc_id.v_required.err = Please enter the Woman's ANC ID\n" +
            "anc_register.step1.last_name.hint =";
    
    public static final String JSON_FORM_FILE = "{\"count\":\"1\",\"encounter_type\":\"AEFI\",\"entity_id\":\"\",\"metadata\":{\"start\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"start\",\"openmrs_entity_id\":\"163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"end\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"end\",\"openmrs_entity_id\":\"163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"today\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"encounter\",\"openmrs_entity_id\":\"encounter_date\"},\"deviceid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"deviceid\",\"openmrs_entity_id\":\"163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"subscriberid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"subscriberid\",\"openmrs_entity_id\":\"163150AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"simserial\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"simserial\",\"openmrs_entity_id\":\"163151AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"phonenumber\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"phonenumber\",\"openmrs_entity_id\":\"163152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"encounter_location\":\"\"},\"step1\":{\"title\":\"Adverse Event Reporting\",\"fields\":[{\"key\":\"Reaction_Vaccine\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"6042AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_data_type\":\"select one\",\"type\":\"spinner\",\"hint\":\"Vaccine that caused the reaction\",\"values\":[\"BCG\",\"HepB\",\"OPV\",\"Penta\",\"PCV\",\"Rota\",\"Measles\",\"MR\",\"Yellow Fever\"],\"openmrs_choice_ids\":{\"BCG\":\"149310AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"HepB\":\"162269AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"OPV\":\"129578AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Penta\":\"162265AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"PCV\":\"162266AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Rota\":\"162272AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Measles\":\"149286AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"MR\":\"149286AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Yellow Fever\":\"149253AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"v_required\":{\"value\":true,\"err\":\"Please enter the vaccine that caused the reaction\"}},{\"key\":\"aefi_start_date\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"date_picker\",\"hint\":\"Date the adverse effects began\",\"expanded\":false,\"v_required\":{\"value\":true,\"err\":\"Please enter the date the adverse effects began\"}},{\"key\":\"reaction\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"openmrs_data_type\":\"\",\"type\":\"check_box\",\"label\":\"Select the reaction\",\"hint\":\"Select the reaction\",\"label_text_style\":\"bold\",\"options\":[{\"key\":\"bacteria_abscesses\",\"text\":\"Minor AEFI Bacteria abscesses\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"lymphadenitis\",\"text\":\"Minor AEFI Lymphadenitis\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"sepsis\",\"text\":\"Minor AEFI Sepsis\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"local_reaction\",\"text\":\"Minor AEFI Severe local reaction\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"birth_defect\",\"text\":\"Serious AEFI Birth Defect\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"death\",\"text\":\"Serious AEFI Death\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"encephalopathy\",\"text\":\"Serious AEFI Encephalopathy\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"high_fever \",\"text\":\"Serious AEFI High fever > 38 Degrees Celcius\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"paralysis\",\"text\":\"Serious AEFI Paralysis\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"seizures\",\"text\":\"Serious AEFI Seizures\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"birth_defect\",\"text\":\"Serious AEFI Significant Disability\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"toxic_shock_syndrome\",\"text\":\"Serious AEFI Toxic shock syndrome\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"other\",\"text\":\"Other (specify)\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"}],\"v_required\":{\"value\":false,\"err\":\"Please select at least one reaction\"}},{\"key\":\"other_reaction\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"openmrs_data_type\":\"text\",\"type\":\"edit_text\",\"hint\":\"Other Reaction\",\"relevance\":{\"step1:reaction\":{\"ex-checkbox\":[{\"or\":[\"other\"]}]}}},{\"key\":\"child_referred\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163340AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_data_type\":\"select one\",\"type\":\"native_radio\",\"label\":\"Child Referred?\",\"label_text_style\":\"bold\",\"options\":[{\"key\":\"Yes\",\"text\":\"Yes\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"1267AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"No\",\"text\":\"No\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163339AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]},{\"key\":\"aefi_form\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163340AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_data_type\":\"select one\",\"type\":\"native_radio\",\"label\":\"Was the AEFI form completed?\",\"label_text_style\":\"bold\",\"options\":[{\"key\":\"Yes\",\"text\":\"Yes\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"1267AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"No\",\"text\":\"No\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163339AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]}]}}";
}
