package com.app.restaurante.srv.application.core.entity;

import com.ada.erp.shared.validation.CNPJ;
import com.ada.erp.shared.validation.CnpjSupport;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "restaurante")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 18)
    @NotBlank
    @CNPJ
    private String cnpj;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_endereco", nullable = false, unique = true,
        foreignKey = @ForeignKey(name = "fk_restaurante_endereco"))
    private Endereco endereco;

    @Column(length = 20)
    private String telefone;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_configuracao", nullable = false, unique = true,
        foreignKey = @ForeignKey(name = "fk_restaurante_configuracao"))
    private Configuracao configuracao;

    @PrePersist
    @PreUpdate
    void normalizeCnpj() {
        cnpj = CnpjSupport.normalize(cnpj);
    }
}
