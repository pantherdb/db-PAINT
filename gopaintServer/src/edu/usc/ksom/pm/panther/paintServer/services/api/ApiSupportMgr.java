/**
 * Copyright 2021 University Of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.usc.ksom.pm.panther.paintServer.services.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import edu.usc.ksom.pm.panther.paintServer.services.servlet.NodeAnnotation;
import edu.usc.ksom.pm.panther.paintServer.services.ServiceUtils;
import edu.usc.ksom.pm.panther.paintServer.services.servlet.FamilyHistory;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;

public class ApiSupportMgr {

    private static ApiSupportMgr instance;
    public static final String API_TITLE_PANTHER = "PAINT API";
    public static final String API_VERSION_PANTHER = "2020311";
    public static final String API_DESCRIPTION_PANTHER = "PAINT supported API's";
    
    public static final String TAG_NAME_NODES = "Node information";
    public static final String TAG_DESC_NODES = "Returns information about Nodes within PAINT trees";
    
    public static final String TAG_NAME_FAMILY = "Family information";
    public static final String TAG_DESC_FAMILY = "Returns information about PAINT families";    
    
    public static final String RESPONSE_OK = "200";
    
    public static final String WARNING = "It is recommended that response from previous web service request is received before sending a new request.  Failure to comply with this policy may result in the IP address being blocked from accessing the PAINT server.";

    public static final String API_PATH_NODE_GO_EVIDENCE = "/services/tree/node/annotation/go/{" + NodeAnnotation.ID + "}";
    public static final String API_PATH_DESCRIPTION_GO_EVIDENCE = "URL for accessing GO annotations for a specified node";
    public static final String API_DESCRIPTION_NODE_GO_EVIDENCE = "The " + API_PATH_NODE_GO_EVIDENCE + " web service can be used to retrieve the list of GO annotations associated with a specified node.\n" + WARNING;
    public static final String API_DESCRIPTION_RESPONSE_NODE_GO_EVIDENCE = "The service returns information about GO annotations to the specified node.";

    public static final String API_PATH_FAMILY_UPDATE_HISTORY = "/services/family/updateHistory/{" + FamilyHistory.ID + "}";    
    public static final String API_PATH_DESCRIPTION_FAMILY_UPDATE_HISTORY = "URL for accessing Family Update History information";
    public static final String API_DESCRIPTION_FAMILY_UPDATE_HISTORY = "The " + API_PATH_FAMILY_UPDATE_HISTORY + " web service can be used to retrieve information abouut updates to the family.\n" + WARNING;
    public static final String API_DESCRIPTION_RESPONSE_FAMILY_UPDATE_HISTORY = "The service returns information about family update history.";
    
    
    private static OpenAPI apiPaint = null;
    
    public static final String PATH_IN = "path";    

    private ApiSupportMgr() {

    }

    public static synchronized ApiSupportMgr getInst() {
        if (null == instance) {
            instance = new ApiSupportMgr();
        }
        return instance;
    }

    public synchronized OpenAPI getApiPaintServer() {
        if (null != apiPaint) {
            return apiPaint;
        }

        OpenAPI oai = new OpenAPI()
                .info(new Info().title(API_TITLE_PANTHER)
                        .version(API_VERSION_PANTHER)
                        .description(API_DESCRIPTION_PANTHER));
        
        // Tags
        Tag nodesTag = new Tag();
        nodesTag.name(TAG_NAME_NODES);
        nodesTag.description(TAG_DESC_NODES);
        oai.addTagsItem(nodesTag);

        Tag familyTag = new Tag();
        nodesTag.name(TAG_NAME_FAMILY);
        nodesTag.description(TAG_DESC_FAMILY);
        oai.addTagsItem(familyTag);
        
        // Paths
        Paths paths = new Paths();
        oai.setPaths(paths);
        addPathInfoNodeGoAnnots(oai);
        addPathInfoFamilyUpdateHistory(oai);
        
        apiPaint = oai;
        return apiPaint;

    }
    

    private static void addPathInfoNodeGoAnnots(OpenAPI oai) {
        PathItem pi =  new PathItem();
        oai.getPaths().addPathItem(API_PATH_NODE_GO_EVIDENCE, pi);
        pi.setDescription(API_PATH_DESCRIPTION_GO_EVIDENCE);
        Operation op = new Operation();
        op.addTagsItem(TAG_NAME_NODES);
        op.setSummary(API_DESCRIPTION_NODE_GO_EVIDENCE);
        pi.post(op);
        pi.get(op);
        
        // Path
        Parameter id = new Parameter();
        op.addParametersItem(id);
        id.in(PATH_IN);
        id.setName(NodeAnnotation.ID);
        id.setDescription(NodeAnnotation.DESC_PARAM_ID);
        id.setRequired(true);
        id.setExample(NodeAnnotation.SAMPLE_LEAF_PERSISTENT_ID);
        id.setSchema(new StringSchema());
        
        ApiResponses responses = new ApiResponses();
        op.setResponses(responses);      
        ApiResponse response = new ApiResponse();
        responses.addApiResponse(RESPONSE_OK, response);
        
        Content content = new Content();
        response.setContent(content);
        response.setDescription(API_DESCRIPTION_RESPONSE_NODE_GO_EVIDENCE);
        MediaType mt = new MediaType();
        content.addMediaType(ServiceUtils.OUTPUT_TYPE_JSON, mt);
        
    }
    
    private static void addPathInfoFamilyUpdateHistory(OpenAPI oai) {
        PathItem pi =  new PathItem();
        oai.getPaths().addPathItem(API_PATH_FAMILY_UPDATE_HISTORY, pi);
        pi.setDescription(API_PATH_DESCRIPTION_GO_EVIDENCE);
        Operation op = new Operation();
        op.addTagsItem(TAG_NAME_FAMILY);
        op.setSummary(API_DESCRIPTION_FAMILY_UPDATE_HISTORY);
        pi.post(op);
        pi.get(op);
        
        // Path
        Parameter id = new Parameter();
        op.addParametersItem(id);
        id.in(PATH_IN);
        id.setName(NodeAnnotation.ID);
        id.setDescription(FamilyHistory.DESC_PARAM_ID);
        id.setRequired(true);
        id.setExample(FamilyHistory.SAMPLE_FAMILY_ID);
        id.setSchema(new StringSchema());
        
        ApiResponses responses = new ApiResponses();
        op.setResponses(responses);      
        ApiResponse response = new ApiResponse();
        responses.addApiResponse(RESPONSE_OK, response);
        
        Content content = new Content();
        response.setContent(content);
        response.setDescription(API_DESCRIPTION_RESPONSE_FAMILY_UPDATE_HISTORY);
        MediaType mt = new MediaType();
        content.addMediaType(ServiceUtils.OUTPUT_TYPE_JSON, mt);
        
    }
    
    public String getJsonAPIForPaintServer() throws Exception {
        OpenAPI api = getApiPaintServer();

        if (null == api) {
            return null;
        }
        return writeJson(api);        
    }

    public static String writeJson(Object value) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper.writer(new DefaultPrettyPrinter()).writeValueAsString(value);
    }     
}
