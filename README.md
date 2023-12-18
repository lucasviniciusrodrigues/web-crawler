# axreng-test

Este projeto se trata de uma implementação de um crawler disponibilizado através de endpoints REST.

## Começando

Essas instruções permitirão que você obtenha uma cópia do projeto em operação na sua máquina local para fins de desenvolvimento e teste.

### Introduction

A aplicação tem como intuito buscar um termo chave na URL_BASE disponibilizada como variável de ambiente, as consultas devem ser assincronas e implementam multhread para buscas simultaneas.

A chave do json para a palavra chave esta configurada no property, assim como as configurações de retentativa (Máximo de retentativas e delay entre elas).

### Execução

docker build . -t axreng/backend
docker run
-e BASE_URL=http://hiring.axreng.com/
-p 4567:4567 --rm axreng/backend

### Tests

Nos testes a classe Mock é tem o intuito encapsular as classes de contrato mockadas para realização dos testes, enquando a classe asserts encapsula o assert das mesmas.

## Construído com

* [Maven](https://maven.apache.org/) - Gerente de Dependência

## Autores

* **Lucas Vinicius Salviano Rodrigues** - *Desenvolvimento* - [Git Lucas](https://github.com/lucasviniciusrodrigues)

## Gratitude

* Independente do resultado, agradeço pelo teste, foi divertido