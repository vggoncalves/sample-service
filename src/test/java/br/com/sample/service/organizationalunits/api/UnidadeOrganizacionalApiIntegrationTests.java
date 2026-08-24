package br.com.sample.service.organizationalunits.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("local-sqlite")
@AutoConfigureMockMvc
@SpringBootTest
class UnidadeOrganizacionalApiIntegrationTests {
    private static final Path DATABASE = Path.of("target", "sqlite-api-" + UUID.randomUUID() + ".db");
    private static final String BASE = "/api/unidadesOrganizacionais/v1.0.0/unidadesOrganizacionais";

    @Autowired private MockMvc mockMvc;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + DATABASE);
    }

    @Test
    void criaEConsultaUnidade() throws Exception {
        var resposta = mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("""
                {"codigo":" dir-fin ","nome":"Diretoria Financeira","sigla":"difin","tipo":"DIRETORIA"}
                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString(BASE + "/")))
                .andExpect(jsonPath("$.codigo").value("DIR-FIN"))
                .andExpect(jsonPath("$.sigla").value("DIFIN"))
                .andExpect(jsonPath("$.ativa").value(true))
                .andReturn();

        var id = com.jayway.jsonpath.JsonPath.read(resposta.getResponse().getContentAsString(), "$.id");
        mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Diretoria Financeira"));
    }

    @Test
    void retornaErrosPadronizadosParaDuplicidadeEntradaInvalidaEInexistencia() throws Exception {
        var corpo = "{\"codigo\":\"DUP-01\",\"nome\":\"Unidade Duplicada\",\"tipo\":\"OUTRA\"}";
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isCreated());
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error[0].codigoErro").value("UNIDADE-0001"));
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"X\",\"nome\":\"\",\"tipo\":null}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error[0].codigoErro").value("UNIDADE-0002"));
        mockMvc.perform(get(BASE + "/" + UUID.randomUUID()))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.error[0].codigoErro").value("UNIDADE-0003"));
    }

    @Test
    void listaComFiltroPaginacaoEOrdenacaoControlada() throws Exception {
        criar("FIL-01", "Financeiro Central");
        criar("FIL-02", "Financeiro Regional");

        mockMvc.perform(get(BASE).param("nome", "financeiro").param("pn", "0").param("ps", "1")
                .param("sort", "-codigo"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.conteudo.length()").value(1))
                .andExpect(jsonPath("$.conteudo[0].codigo").value("FIL-02"))
                .andExpect(jsonPath("$.totalElementos").value(2)).andExpect(jsonPath("$.totalPaginas").value(2));
        mockMvc.perform(get(BASE).param("ps", "101"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error[0].codigoErro").value("UNIDADE-0008"));
        mockMvc.perform(get(BASE).param("sort", "inexistente"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error[0].codigoErro").value("UNIDADE-0008"));
    }

    @Test
    void alteraSituacaoERejeitaVersaoDesatualizada() throws Exception {
        var id = criarId("ALT-01", "Unidade Alterável", null);

        mockMvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Unidade Alterada","sigla":"UAL","tipo":"OUTRA","versao":0}
                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Unidade Alterada"))
                .andExpect(jsonPath("$.versao").value(1));
        mockMvc.perform(patch(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"ativa\":false,\"versao\":1}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativa").value(false));
        mockMvc.perform(patch(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"ativa\":true,\"versao\":1}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error[0].codigoErro").value("UNIDADE-0009"));
    }

    @Test
    void consultaFilhasArvoreEDesativacaoRespeitaDescendentes() throws Exception {
        var raiz = criarId("RAIZ-07", "Raiz da Árvore", null);
        criarId("FILHA-07", "Filha da Árvore", raiz);

        mockMvc.perform(get(BASE + "/" + raiz + "/filhas"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElementos").value(1));
        mockMvc.perform(get(BASE + "/arvore").param("raizId", raiz).param("profundidade", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].unidade.id").value(raiz))
                .andExpect(jsonPath("$[0].filhas.length()").value(1));
        mockMvc.perform(get(BASE + "/arvore").param("profundidade", "0"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error[0].codigoErro").value("UNIDADE-0014"));
        mockMvc.perform(delete(BASE + "/" + raiz).header("If-Match", "0"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error[0].codigoErro").value("UNIDADE-0007"));
    }

    private void criar(String codigo, String nome) throws Exception {
        criarId(codigo, nome, null);
    }

    private String criarId(String codigo, String nome, String unidadePaiId) throws Exception {
        var pai = unidadePaiId == null ? "" : ",\"unidadePaiId\":\"" + unidadePaiId + "\"";
        var resposta = mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"" + codigo + "\",\"nome\":\"" + nome + "\",\"tipo\":\"OUTRA\"" + pai + "}"))
                .andExpect(status().isCreated()).andReturn();
        return com.jayway.jsonpath.JsonPath.read(resposta.getResponse().getContentAsString(), "$.id");
    }
}
