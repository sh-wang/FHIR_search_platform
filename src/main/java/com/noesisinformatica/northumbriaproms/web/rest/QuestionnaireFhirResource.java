package com.noesisinformatica.northumbriaproms.web.rest;

/*-
 * #%L
 * Proms Platform
 * %%
 * Copyright (C) 2017 - 2018 Termlex
 * %%
 * This software is Copyright and Intellectual Property of Termlex Inc Limited.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation as version 3 of the
 * License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/agpl-3.0.en.html>.
 * #L%
 */

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.codahale.metrics.annotation.Timed;
import com.noesisinformatica.northumbriaproms.domain.Questionnaire;
import com.noesisinformatica.northumbriaproms.service.QuestionnaireService;
import com.noesisinformatica.northumbriaproms.web.rest.util.PaginationUtil;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



/**
 * REST controller for Resource QuestionnaireResponse.
 */
@RestController
@RequestMapping("/api/fhir")
public class QuestionnaireFhirResource {
    private final Logger log = LoggerFactory.getLogger(QuestionnaireFhirResource.class);
    private final QuestionnaireService questionnaireService;

    public QuestionnaireFhirResource(QuestionnaireService questionnaireService){
        this.questionnaireService = questionnaireService;
    }

    private final String defaultPath = "localhost:8080/api/fhir/";


    /**
     * GET  /questionnaires/:id : get the "id" questionnaire.
     *
     * @param id the id of the questionnaire to retrieve
     * @return corresponding questionnaire in FHIR
     */
    @GetMapping("/Questionnaire/{id}")
    @Timed
    public String getQuestionnaire(@PathVariable Long id) {
        log.debug("REST request to get Questionnaire in FHIR : {}", id);
        org.hl7.fhir.dstu3.model.Questionnaire questionnaireFhir = getQuestionnaireResource(id);

        FhirContext ctx = FhirContext.forDstu3();
        IParser p =ctx.newJsonParser();
        p.setPrettyPrint(false);
        String encode = p.encodeResourceToString(questionnaireFhir);
        return encode;
    }


    /**
     * get the FHIR dstu3 questionnaire with ID id
     *
     * @param id the id of the questionnaire to retrieve
     * @return corresponding FHIR dstu3 questionnaire
     */
    public org.hl7.fhir.dstu3.model.Questionnaire getQuestionnaireResource(@PathVariable Long id){
        Questionnaire questionnaire = questionnaireService.findOne(id);
        org.hl7.fhir.dstu3.model.Questionnaire questionnaireFhir = new org.hl7.fhir.dstu3.model.Questionnaire();

        questionnaireFhir.setUrl(defaultPath + "questionnaires/"+id);
        questionnaireFhir.setId(id.toString());
        questionnaireFhir.setStatus(Enumerations.PublicationStatus.ACTIVE);
        questionnaireFhir.setName(questionnaire.getName());
        questionnaireFhir.setCopyright(questionnaire.getCopyright());

        return questionnaireFhir;
    }


    /**
     * GET  /questionnaires : get all the questionnaires.
     *
     * @param pageable the pagination information
     * @return all questionnaires in FHIR
     */
    @GetMapping("/Questionnaire/all")
    @Timed
    public String getAllQuestionnaires(Pageable pageable) {
        log.debug("REST request to get a page of Questionnaires in FHIR");
        Page<Questionnaire> page = questionnaireService.findAll(pageable);

        String questionnaires = "[";
        int i, questionCount;
        questionCount = page.getContent().size();
        if (questionCount == 0){ return "[]";}
        for (i = 0; i < questionCount - 1; i++){
            questionnaires = questionnaires + getQuestionnaire(page.getContent().get(i).getId()) + ",";
        }

        questionnaires = questionnaires + getQuestionnaire(page.getContent().get(i).getId()) + "]";
        return questionnaires;
    }
}
