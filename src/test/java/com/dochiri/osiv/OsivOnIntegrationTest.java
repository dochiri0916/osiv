package com.dochiri.osiv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.dochiri.osiv.repository.PostRepository;

@SpringBootTest(
        properties = "spring.jpa.open-in-view=true"
)
@AutoConfigureMockMvc
class OsivOnIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Test
    void returnsPostsWhenOsivIsOn() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/osiv/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("comments");
    }

    @Test
    void returnsPostsByIdReferenceWhenOsivIsOn() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/osiv/posts-id-reference"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("식별자 참조 설계");
    }

    @Test
    void createsCommentWithIdReferenceWhenOsivIsOn() throws Exception {
        Long postId = postRepository.findAll().getFirst().getId();

        MvcResult result = mockMvc.perform(post("/api/osiv/comments/id-reference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postId": %d,
                                  "content": "id reference command"
                                }
                                """.formatted(postId)))
                .andExpect(status().isCreated())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("id-reference");
    }
}
