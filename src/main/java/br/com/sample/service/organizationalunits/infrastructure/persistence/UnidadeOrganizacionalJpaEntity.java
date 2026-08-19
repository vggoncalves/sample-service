package br.com.sample.service.organizationalunits.infrastructure.persistence;

import br.com.sample.service.organizationalunits.domain.model.TipoUnidade;
import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacionalPersistida;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "unidade_organizacional")
class UnidadeOrganizacionalJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36, updatable = false)
    private String id;

    @Column(name = "codigo", nullable = false, length = 50, unique = true, updatable = false)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "sigla", length = 30)
    private String sigla;

    @Column(name = "descricao", length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoUnidade tipo;

    @Column(name = "unidade_pai_id", length = 36)
    private String unidadePaiId;

    @Column(name = "email_contato", length = 254)
    private String emailContato;

    @Column(name = "telefone", length = 16)
    private String telefone;

    @Column(name = "ativa", nullable = false)
    private boolean ativa;

    @Column(name = "criado_em", nullable = false, length = 35, updatable = false)
    private String criadoEm;

    @Column(name = "criado_por", nullable = false, length = 255, updatable = false)
    private String criadoPor;

    @Column(name = "atualizado_em", nullable = false, length = 35)
    private String atualizadoEm;

    @Column(name = "atualizado_por", nullable = false, length = 255)
    private String atualizadoPor;

    @Version
    @Column(name = "versao", nullable = false)
    private long versao;

    protected UnidadeOrganizacionalJpaEntity() {
    }

    static UnidadeOrganizacionalJpaEntity de(UnidadeOrganizacionalPersistida unidade) {
        var entidade = new UnidadeOrganizacionalJpaEntity();
        entidade.id = unidade.id().toString();
        entidade.codigo = unidade.codigo();
        entidade.nome = unidade.nome();
        entidade.sigla = unidade.sigla();
        entidade.descricao = unidade.descricao();
        entidade.tipo = unidade.tipo();
        entidade.unidadePaiId = unidade.unidadePaiId() == null ? null : unidade.unidadePaiId().toString();
        entidade.emailContato = unidade.emailContato();
        entidade.telefone = unidade.telefone();
        entidade.ativa = unidade.ativa();
        entidade.criadoEm = unidade.criadoEm().toString();
        entidade.criadoPor = unidade.criadoPor();
        entidade.atualizadoEm = unidade.atualizadoEm().toString();
        entidade.atualizadoPor = unidade.atualizadoPor();
        entidade.versao = unidade.versao();
        return entidade;
    }

    String getId() { return id; }
    String getCodigo() { return codigo; }
    String getNome() { return nome; }
    String getSigla() { return sigla; }
    String getDescricao() { return descricao; }
    TipoUnidade getTipo() { return tipo; }
    String getUnidadePaiId() { return unidadePaiId; }
    String getEmailContato() { return emailContato; }
    String getTelefone() { return telefone; }
    boolean isAtiva() { return ativa; }
    String getCriadoEm() { return criadoEm; }
    String getCriadoPor() { return criadoPor; }
    String getAtualizadoEm() { return atualizadoEm; }
    String getAtualizadoPor() { return atualizadoPor; }
    long getVersao() { return versao; }
}
