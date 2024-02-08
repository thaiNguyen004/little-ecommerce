package thainguyen.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static thainguyen.domain.Constants.SEQUENCE_GENERATOR;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Address {
    @Id
    @GeneratedValue(generator = SEQUENCE_GENERATOR)
    private Long id;

    @NotBlank(message = "phoneNumber attribute must not be null")
    private String phoneNumber;
    @NotBlank(message = "province attribute must not be null")
    private String province;
    @NotBlank(message = "district attribute must not be null")
    private String district;
    @NotBlank(message = "ward attribute must not be null")
    private String ward;
    @NotBlank(message = "detailAddress attribute must not be null")
    private String detailAddress;

    /*Custom constructor*/
    public Address(String phoneNumber, String province
            , String district, String ward, String address) {
        this.phoneNumber = phoneNumber;
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.detailAddress = address;
    }

}
