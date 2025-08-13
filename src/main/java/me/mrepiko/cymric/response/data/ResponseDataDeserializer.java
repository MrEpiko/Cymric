package me.mrepiko.cymric.response.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;

public class ResponseDataDeserializer extends JsonDeserializer<ResponseData> {

    @Override
    public ResponseData deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        JsonToken currentToken = jsonParser.currentToken();

        ResponseData responseData = new ResponseData();

        if (currentToken == JsonToken.START_OBJECT) {
            ActionData action = mapper.readValue(jsonParser, ActionData.class);
            responseData.add(action);
        } else if (currentToken == JsonToken.START_ARRAY) {
            ActionData[] actions = mapper.readValue(jsonParser, ActionData[].class);
            responseData.addAll(Arrays.asList(actions));
        } else {
            throw new RuntimeException("Unrecognized token: " + currentToken);
        }

        return responseData;
    }

}
