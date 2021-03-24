package ai.djl.spring.examples.webapp.data.pojo;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class InferenceResponse {
    private List<InferredObject> objects;
    private String outputReference;
}
