# 📚 My Book Tracker

O **My Book Tracker** é uma aplicação desktop desenvolvida em **Java puro (Swing)** para ajudar leitores a organizarem sua biblioteca pessoal.  
O projeto permite:

- Cadastrar livros físicos ou digitais  
- Gerenciar status de leitura  
- Filtrar por gênero  
- Salvar citações favoritas  
- Persistir todos os dados em arquivos `.txt` legíveis

## 📸 Telas do Projeto

- Tela Principal  
- Detalhes do Livro  
- Cadastro de Livro  
- Nova Citação  

## 🚀 Funcionalidades Atuais

### **Gerenciamento de Livros**
- Cadastro de livros: **Título, Autor, Editora, Páginas, Descrição**
- Diferenciação via **polimorfismo** entre:
  - **Livro Físico**
  - **Ebook** 
- Atualização do progresso de leitura (página atual)
- Alteração de status: **A Ler**, **Lendo**, **Lido**
- Exclusão de livros

### **Organização**
- Cadastro dinâmico de gêneros
- Filtro por:
  - **Gênero**
  - **Status de Leitura**
- Busca por título ou autor

### **Citações**
- Adicionar citações por livro
- Visualizar lista de citações associadas

### **Persistência de Dados**
- Salvamento automático nos arquivos:
  - `books.txt`
  - `genres.txt`
- Formato customizado e legível, com uso de **tags de proteção de dados**

## 🛠️ Tecnologias e Conceitos Aplicados

Este projeto foi desenvolvido aplicando amplamente conceitos de **POO – Programação Orientada a Objetos**.

### **Tecnologias:**
- **Java** (JDK 21+ recomendado)
- **Java Swing** (JFrame, JPanel, JDialog)
- IDEs: **Apache NetBeans (GUI Builder)** e **Eclipse**

### **Conceitos de POO:**
- **Herança:**  
  Classe abstrata `Book` estendida por `PhysicalBook` e `Ebook`
- **Polimorfismo:**  
  Listas genéricas de livros, com comportamento individual no salvamento/carregamento
- **Encapsulamento:**  
  Uso de getters, setters e modificadores adequados
- **Estruturas de Dados:**  
  Uso de `List`, `ArrayList` e `Streams` para filtragem
- **Tratamento de Exceções:**  
  Exceções personalizadas (`ValidationException`)
- **Persistência:**  
  Leitura/escrita com `BufferedReader` e `BufferedWriter`

## 📂 Estrutura do Projeto

Padrão **MVC simplificado**:

src/com/bookTracker/
├── exception/ # Exceções personalizadas (ValidationException)
├── gui/ # Telas e componentes Swing (MainFrame, Panels)
├── model/ # Classes de domínio (Book, Genre, Ebook, etc.)
├── persistence/ # Lógica de arquivos (DataManager)
└── service/ # Regras de negócio e controle (BookService)

## 🔮 Roadmap (Futuras Melhorias)

- [ ] **Datas de Leitura:** Início e término da leitura  
- [ ] **Sistema de Rating:** Avaliação de 0 a 5 estrelas  
- [ ] **Filtro por Avaliação:** Exibir só os melhores avaliados  
- [ ] **Notas Pessoais:** Campo de texto para resenhas ou anotações  
- [ ] **Dashboard:** Estatísticas como total de páginas lidas e gênero favorito  
- [ ] **Backup:** Exportação dos arquivos `.txt`  

## 🤝 Contribuição

Contribuições são bem-vindas! Para colaborar:

1. Faça um **Fork**
2. Crie uma branch:  
   `git checkout -b feature/NovaFeature`
3. Commit:  
   `git commit -m "Adicionando nova feature"`
4. Push:  
   `git push origin feature/NovaFeature`
5. Abra um **Pull Request**

---

Desenvolvido por Rubens Schueng Netto 🚀
