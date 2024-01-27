package thainguyen.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
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

    private String phoneNumber;
    private @NotNull String province;
    private @NotNull String district;
    private @NotNull String ward;
    private String detailAddress;
    private @NotNull String hamlet;

    /*Custom constructor*/
    public Address(String phoneNumber, String province
            , String district, String ward, String address, String hamlet) {
        this.phoneNumber = phoneNumber;
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.detailAddress = address;
        this.hamlet = hamlet;
    }

}
