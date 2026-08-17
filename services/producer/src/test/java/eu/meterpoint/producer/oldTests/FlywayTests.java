package eu.meterpoint.producer.oldTests;

//import eu.meterpoint.producer.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
//@Import(TestcontainersConfiguration.class)
class FlywayTests {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void flywayAppliedOutboxSchema() {
        Integer tables = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'outbox'",
                Integer.class);
        assertThat(tables).isEqualTo(1);

        String indexDef = jdbcTemplate.queryForObject(
                "select indexdef from pg_indexes where indexname = 'outbox_unpublished_idx'",
                String.class);
        assertThat(indexDef).contains("WHERE (published_at IS NULL)");
    }
}
