package thainguyen.utility.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseComponent<T> {
    private boolean success;
    private HttpStatus status;
    private String message;
    private List<String> errors;
    private T data;
}