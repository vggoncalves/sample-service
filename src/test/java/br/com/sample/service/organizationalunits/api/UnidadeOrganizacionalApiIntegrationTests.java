package br.com.sample.service.organizationalunits.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithAnonymousUser;

@ActiveProfiles("local-sqlite")
@AutoConfigureMockMvc
@SpringBootTest
@WithMockUser(username = "admin", authorities = {"UNIDADE_CONSULTAR", "UNIDADE_CRIAR", "UNIDADE_ALTERAR", "UNIDADE_DESATIVAR"})
class UnidadeOrganizacionalApiIntegrationTests {
    private static final Path DATABASE = Path.of("target", "sqlite-api-" + UUID.randomUUID() + ".db");
    private static final String BASE = "/api/unidadesOrganizacionais/v1.0.0/unidadesOrganizacionais";
    private static final String SENHA_ADMIN_LOCAL = UUID.randomUUID().toString();
    private static final String SENHA_CONSULTA_LOCAL = UUID.randomUUID().toString();

    @Autowired private MockMvc mockMvc;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + DATABASE);
        registry.add("SAMPLE_LOCAL_ADMIN_PASSWORD", () -> SENHA_ADMIN_LOCAL);
        registry.add("SAMPLE_LOCAL_CONSULTA_PASSWORD", () -> SENHA_CONSULTA_LOCAL);
    }

    @Test
    void criaEConsultaUnidade() throws Exception {
        var resposta = mockMvc.perform(post(BASE).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("""
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
        mockMvc.perform(post(BASE).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());
        mockMvc.perform(post(BASE).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error[0].codigoErro").value("UNIDADE-0001"));
        mockMvc.perform(post(BASE).with(csrf()).header("X-Correlation-Id", "correlation-erro-9")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"X\",\"nome\":\"\",\"tipo\":null}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error[0].codigoErro").value("UNIDADE-0002"))
                .andExpect(jsonPath("$.correlationId").value("correlation-erro-9"));
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

        mockMvc.perform(put(BASE + "/" + id).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Unidade Alterada","sigla":"UAL","tipo":"OUTRA","versao":0}
                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Unidade Alterada"))
                .andExpect(jsonPath("$.versao").value(1));
        mockMvc.perform(patch(BASE + "/" + id).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"ativa\":false,\"versao\":1}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativa").value(false));
        mockMvc.perform(patch(BASE + "/" + id).with(csrf()).contentType(MediaType.APPLICATION_JSON)
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
        mockMvc.perform(delete(BASE + "/" + raiz).with(csrf()).header("If-Match", "0"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error[0].codigoErro").value("UNIDADE-0007"));
    }

    @Test
    @WithAnonymousUser
    void rejeitaUsuarioNaoAutenticado() throws Exception {
        mockMvc.perform(get(BASE)).andExpect(status().isUnauthorized());
    }

    @Test
    void propagaCorrelationIdNoCabecalhoDaResposta() throws Exception {
        mockMvc.perform(get("/csrf").header("X-Correlation-Id", "correlation-smoke-9"))
                .andExpect(status().isOk()).andExpect(header().string("X-Correlation-Id", "correlation-smoke-9"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void expoeMetricasEContratoOpenApiParaClienteAutenticado() throws Exception {
        mockMvc.perform(get("/actuator/metrics")).andExpect(status().isOk()).andExpect(jsonPath("$.names").isArray());
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API de Unidades Organizacionais"))
                .andExpect(jsonPath("$.components.securitySchemes.basicAuth.scheme").value("basic"));
    }

    @Test
    @WithMockUser(username = "consulta", authorities = "UNIDADE_CONSULTAR")
    void rejeitaUsuarioSemPermissaoDeCriar() throws Exception {
        mockMvc.perform(post(BASE).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"SEM-01\",\"nome\":\"Sem Permissão\",\"tipo\":\"OUTRA\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void aplicaIdentidadesLocaisComAutenticacaoBasica() throws Exception {
        mockMvc.perform(post(BASE).with(httpBasic("admin", SENHA_ADMIN_LOCAL)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"BAS-01\",\"nome\":\"Autenticação Básica\",\"tipo\":\"OUTRA\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post(BASE).with(httpBasic("consulta", SENHA_CONSULTA_LOCAL)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"BAS-02\",\"nome\":\"Sem Escrita\",\"tipo\":\"OUTRA\"}"))
                .andExpect(status().isForbidden());
    }

    private void criar(String codigo, String nome) throws Exception {
        criarId(codigo, nome, null);
    }

    private String criarId(String codigo, String nome, String unidadePaiId) throws Exception {
        var pai = unidadePaiId == null ? "" : ",\"unidadePaiId\":\"" + unidadePaiId + "\"";
        var resposta = mockMvc.perform(post(BASE).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"" + codigo + "\",\"nome\":\"" + nome + "\",\"tipo\":\"OUTRA\"" + pai + "}"))
                .andExpect(status().isCreated()).andReturn();
        return com.jayway.jsonpath.JsonPath.read(resposta.getResponse().getContentAsString(), "$.id");
    }
}
