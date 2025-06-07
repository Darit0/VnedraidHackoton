package vnedraid.inputservice.api.hh.dto;

import lombok.Data;

@Data
public class AddressDto {
    private String city;
    private String street;
    private String building;
    private String lat;
    private String lon;
}
