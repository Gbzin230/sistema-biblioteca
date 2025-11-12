package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("FUNCIONARIO")
public class Funcionario extends Pessoa {
}
