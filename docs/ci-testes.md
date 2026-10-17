# CI, Checkstyle e testes (J-10)

O projeto usa Maven Wrapper, Checkstyle e testes JUnit para validar alterações localmente e no GitHub Actions. O workflow está em [`.github/workflows/build.yml`](../.github/workflows/build.yml).

## Pré-requisitos e validação local

Use um JDK 25, conforme `java.version` no [`pom.xml`](../application/pom.xml). O Maven Wrapper baixa a versão de Maven definida no repositório; a instalação global de Maven é opcional.

No PowerShell, a partir da raiz do repositório:

```powershell
java -version
.\application\mvnw.cmd -v
.\application\mvnw.cmd -f application\pom.xml --batch-mode --no-transfer-progress clean test
```

Confira a versão de Java mostrada pelo Wrapper. Se necessário, selecione o JDK instalado apenas para a sessão atual, adaptando o caminho:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25'
.\application\mvnw.cmd -v
```

No Linux/macOS, a partir da raiz:

```bash
cd application
bash mvnw --batch-mode --no-transfer-progress clean test
```

`clean test` remove o build anterior, executa Checkstyle, compila e roda os testes. Para executar também o empacotamento, como no CI, substitua `test` por `verify`.

Com Maven global instalado e JDK 25 selecionado, `mvn clean test` e `mvn clean verify` podem ser executados dentro de `application`.

As configurações em [`src/test/resources/application.properties`](../application/src/test/resources/application.properties) usam H2 em memória e uma chave JWT exclusiva de testes. Essa suíte roda sem subir Docker, MySQL ou RabbitMQ e sem fornecer credenciais de produção. O modo de compatibilidade do H2 permite carregar as migrations atuais, mas não substitui testes de integração específicos com MySQL.

## Checkstyle

O plugin está vinculado à fase `validate`, que ocorre antes de `test`. Assim, `mvn test` também executa a análise de estilo; configurar o plugin apenas em `verify` não atenderia a esse fluxo.

Trecho da configuração no `pom.xml`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.6.0</version>
    <configuration>
        <configLocation>${project.basedir}/checkstyle.xml</configLocation>
        <includeTestSourceDirectory>true</includeTestSourceDirectory>
        <consoleOutput>true</consoleOutput>
        <failOnViolation>true</failOnViolation>
        <violationSeverity>error</violationSeverity>
    </configuration>
    <executions>
        <execution>
            <id>checkstyle</id>
            <phase>validate</phase>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

As regras em [`application/checkstyle.xml`](../application/checkstyle.xml) incluem imports sem wildcard e sem itens não utilizados, chaves em blocos, nomes Java e limite de 120 caracteres por linha. As violações estão configuradas com severidade `error` e interrompem o build, inclusive em fontes de teste.

Para executar somente Checkstyle, a partir da raiz no PowerShell:

```powershell
.\application\mvnw.cmd -f application\pom.xml --batch-mode --no-transfer-progress validate
```

O relatório fica em `application/target/checkstyle-result.xml`.

## Organização dos testes

| Classe | Escopo |
| --- | --- |
| `AuthenticationControllerTest` | `@WebMvcTest`: contrato HTTP do login, respostas 200/400/401 e dependências mockadas, com segurança ativa |
| `ApplicationTests` | `@SpringBootTest`: carregamento do contexto completo |
| `AuthTests` | `@SpringBootTest` + `@AutoConfigureMockMvc`: smoke test do login, JWT real e endpoint protegido, com repositórios mockados |
| `OpenApiTests` | Documentação OpenAPI e acesso público ao Swagger |

`@WebMvcTest` carrega uma fatia do contexto MVC sem iniciar servidor HTTP nem JPA. Ele é um teste de integração da camada web. O smoke test usa o contexto completo com H2 e verifica se os componentes de autenticação funcionam em conjunto.

O projeto usa Spring Boot 4.1. Nessa versão, `@MockBean` foi removido em favor de `@MockitoBean`; essa é a adaptação necessária ao texto original da J-10. Os imports são:

```java
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
```

O `FuncionarioRepository` é utilizado por `CustomUserDetailsService`. Por isso, o teste MVC importa esse serviço e `SecurityConfig`, e mocka os repositórios e os colaboradores JWT. Os filtros continuam ativos para validar que o login é público.

Exemplo com MockMvc, com os demais imports omitidos. A implementação completa está em [`AuthenticationControllerTest.java`](../application/src/test/java/com/app/template/_1/restaurante/srv/application/controller/AuthenticationControllerTest.java).

```java
@WebMvcTest(AuthenticationController.class)
@Import({SecurityConfig.class, CustomUserDetailsService.class})
@MockitoBean(types = {CargoFuncionarioRepository.class, JwtUtils.class, JwtDecoder.class})
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FuncionarioRepository funcionarioRepository;

    @Test
    void unknownEmployeeReturnsUnauthorized() throws Exception {
        when(funcionarioRepository.findByLogin("inexistente@restaurante.com")).thenReturn(null);

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"inexistente@restaurante.com","senha":"senha-teste"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Credenciais inválidas"));

        verify(funcionarioRepository).findByLogin("inexistente@restaurante.com");
    }
}
```

Além do status, o exemplo verifica a mensagem do controller e a consulta ao repositório. Isso evita que uma rejeição antecipada pelo filtro de segurança seja confundida com a resposta esperada do login.

No smoke test, a permissão `ROLE_USER` é verificada pela presença na lista, sem depender da posição. O Spring Security também pode incluir authorities que identificam o fator de autenticação, como `FACTOR_BEARER`:

```java
import static org.hamcrest.Matchers.hasItem;

// Após chamar o endpoint protegido com o JWT emitido pelo login:
.andExpect(jsonPath("$.authorities").value(hasItem("ROLE_USER")));
```

Para executar somente os testes do controller ou do smoke test, a partir da raiz no PowerShell:

```powershell
.\application\mvnw.cmd -f application\pom.xml -Dtest=AuthenticationControllerTest test
.\application\mvnw.cmd -f application\pom.xml -Dtest=AuthTests test
```

Os relatórios JUnit ficam em `application/target/surefire-reports/`. Uma falha de teste ou de Checkstyle deve ser corrigida antes do merge.

## GitHub Actions

O workflow `Build e qualidade Java` executa em pushes e pull requests. O job `build` usa:

- runner Ubuntu;
- JDK 25 Temurin;
- cache Maven baseado em `application/pom.xml`;
- diretório de execução `application`;
- `bash mvnw --batch-mode --no-transfer-progress clean verify`.

O build falha quando Checkstyle, compilação, testes ou empacotamento falham. Essa execução cobre a preparação de CI da J-10. O deploy contínuo depende da definição do ambiente de destino em uma etapa posterior.

Depois da primeira execução do workflow na branch de destino, um administrador pode exigir o check `build` nas regras de proteção de `develop` e `main`, conforme a política do repositório. Essa configuração é feita em **Settings → Rules → Rulesets** ou **Settings → Branches → Branch protection rules**, selecionando o check de status do workflow antes de permitir o merge.

## Referências

- [GitHub Actions com Java e Maven](https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-maven)
- [Configuração do Maven Checkstyle Plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/usage.html)
- [Testes de aplicações Spring Boot](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html)
- [Migração para Spring Boot 4: remoção de `@MockBean`](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide#mockbean-and-spybean-removal)
