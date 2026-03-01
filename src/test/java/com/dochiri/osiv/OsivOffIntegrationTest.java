package com.dochiri.osiv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dochiri.osiv.p6spy.P6SpyQueryCounter;
import com.dochiri.osiv.repository.PostRepository;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(
        properties = "spring.jpa.open-in-view=false"
)
@AutoConfigureMockMvc
class OsivOffIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Test
    void throwsLazyInitializationExceptionWhenOsivIsOff() {
        assertThatThrownBy(() -> mockMvc.perform(get("/api/osiv/posts")).andReturn())
                .hasRootCauseInstanceOf(LazyInitializationException.class);
    }

    @Test
    void returnsPostsByIdReferenceWhenOsivIsOff() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/osiv/posts-id-reference"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("OSIV 없이 안전한 조회");
    }

    @Test
    void createsCommentWithIdReferenceWhenOsivIsOff() throws Exception {
        Long postId = postRepository.findAll().getFirst().getId();

        MvcResult result = mockMvc.perform(post("/api/osiv/comments/id-reference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postId": %d,
                                  "content": "id reference command off"
                                }
                                """.formatted(postId)))
                .andExpect(status().isCreated())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("id-reference");
    }

    @Test
    void createsCommentWithEntityLoadWhenOsivIsOff() throws Exception {
        Long postId = postRepository.findAll().getFirst().getId();

        MvcResult result = mockMvc.perform(post("/api/osiv/comments/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postId": %d,
                                  "content": "entity load command off"
                                }
                                """.formatted(postId)))
                .andExpect(status().isCreated())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("entity-load");
    }

    @Test
    void comparesSqlCountBetweenEntityLoadAndIdReference() throws Exception {
        Long postId = postRepository.findAll().getFirst().getId();

        P6SpyQueryCounter.reset();
        mockMvc.perform(post("/api/osiv/comments/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postId": %d,
                                  "content": "entity load sql count"
                                }
                                """.formatted(postId)))
                .andExpect(status().isCreated());
        int entityLoadSqlCount = P6SpyQueryCounter.get();

        P6SpyQueryCounter.reset();
        mockMvc.perform(post("/api/osiv/comments/id-reference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postId": %d,
                                  "content": "id reference sql count"
                                }
                                """.formatted(postId)))
                .andExpect(status().isCreated());
        int idReferenceSqlCount = P6SpyQueryCounter.get();

        System.out.printf("SQL count comparison | entity-load=%d, id-reference=%d%n", entityLoadSqlCount, idReferenceSqlCount);

        assertThat(entityLoadSqlCount).isEqualTo(2);
        assertThat(idReferenceSqlCount).isEqualTo(1);
    }
}
