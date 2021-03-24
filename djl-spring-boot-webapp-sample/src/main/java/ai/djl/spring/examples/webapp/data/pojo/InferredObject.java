package ai.djl.spring.examples.webapp.data.pojo;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class InferredObject {
    private String objectClass;
    private Double confidence;
}
