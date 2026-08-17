package eu.meterpoint.producer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class MigrationTest {

    @Autowired
    private JdbcTemplate jdbc;


    // -------- Migrations applied --------
    //TODO: Create array which masked the needed version
    // -> Iterate and check applied for each element of array

    @Test
    void v1IsApplied() {
        Integer applied = jdbc.queryForObject("""
                SELECT count(*) FROM flyway_schema_history
                WHERE version = '1' AND success
                """, Integer.class);

        assertThat(applied).isEqualTo(1);

//        Integer tables = jdbc.queryForObject(
//                "select count(*) from information_schema.tables where table_name = 'outbox'",
//                Integer.class);
//        assertThat(tables).isEqualTo(1);
    }

    @Test
    void v2IsApplied() {
        Integer applied = jdbc.queryForObject("""
                SELECT count(*) FROM flyway_schema_history
                WHERE version = '2' AND success
                """, Integer.class);

        assertThat(applied).isEqualTo(1);
    }


    // -------- Verify partial index --------
    @Test
    void outboxIndexIsPartial () {
        String indexName = "outbox_unpublished_idx";
        String indexDef = jdbc.queryForObject(
                "select indexdef from pg_indexes where indexname = ?",
                String.class,
                indexName);

        assertThat(indexDef)
                .contains("WHERE (published_at IS NULL)");
    }
}