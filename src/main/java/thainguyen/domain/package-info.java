@GenericGenerator(
        name = SEQUENCE_GENERATOR,
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @Parameter(name = "sequence_name", value=SEQUENCE_NAME),
                @Parameter(name = "initital_value", value = INITIAL_VALUE),
                @Parameter(name = "increment", value = INCREMENT_SIZE)
        }
)

package thainguyen.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import static thainguyen.domain.Constants.*;