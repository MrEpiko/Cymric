package me.mrepiko.cymric.config.main;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EndpointData {

    private String endpointUrl;
    private String authorizationHeader = "Authorization";
    private String authorizationToken;

}
