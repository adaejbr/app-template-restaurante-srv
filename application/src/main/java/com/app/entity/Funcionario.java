package com.app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String email;
    private String senha;
    private String cargo;
    private Boolean ativo;

    @Todo
    /* ------------------------------------------------- 
    
    ESSA ENTIDADE FOI CRIADA PARA IMPLEMENTAR O MAPPER, 
    QUANDO FOR FEITA A TASK EM QUE DEVERIA SER CRIADO O FUNCIONÁRIO,
    DEVE-SE OBRIGATORIAMENTE ATUALIZAR O MAPPER PARA QUE O LOGIN FUNCIONE CORRETAMENTE.
    
       ------------------------------------------------- */
}