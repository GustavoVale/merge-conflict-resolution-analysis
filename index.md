# Challenges of Resolving Merge Conflicts: A Mining and Survey Study

Abstract — In collaborative software development, merge conflicts arise when developers integrate concurrent code changes.  Practitioners seek to minimize the number of merge conflicts because resolving them is difficult, time consuming, and often an error-prone task. Despite a substantial number of studies investigating merge conflicts, the challenges in merge conflict resolution are not well understood. Our goal is to investigate which factors make merge conflicts longer to resolve in practice. To this end, we performed a two-phase study. First, we analyzed 66 projects containing around 81 thousand merge scenarios, involving 2 million files and over 10 million chunks. For this analysis, we use rank correlation, principal component analysis, multiple regression model, and effect-size analysis to investigate which independent variables (e.g., number of conflicting chunks and files) mostly influence our dependent variable (i.e., time to merge). We found that the number of chunks, lines of code, conflicting chunks, developers involved, conflicting lines of code, conflicting files, and the complexity of the conflicting code influence the merge conflict resolution time. Second, we surveyed 140 developers from our subject projects aiming at cross-validating our results from the first phase of our study. As main results, (i) we found that committing small chunks makes merge conflict resolution faster when leaving other independent variables untouched, (ii) we found evidence that merge scenario characteristics (e.g., the number of lines of code or chunks changed in the merge scenario) are stronger correlated with our dependent variable than merge conflict characteristics (e.g., the number of lines of code or chunks in conflict), (iii) we devise a taxonomy of four types of challenges in merge conflict resolution, and (iv) we observed that the inherent dependencies among conflicting and non-conflicting code is one of the main factors influencing the merge conflict resolution time.


## Welcome to GitHub Pages

You can use the [editor on GitHub](https://github.com/GustavoVale/merge-conflict-resolution-analysis/edit/gh-pages/index.md) to maintain and preview the content for your website in Markdown files.

Whenever you commit to this repository, GitHub Pages will run [Jekyll](https://jekyllrb.com/) to rebuild the pages in your site, from the content in your Markdown files.

### Markdown

Markdown is a lightweight and easy-to-use syntax for styling your writing. It includes conventions for

```markdown
Syntax highlighted code block

# Header 1
## Header 2
### Header 3

- Bulleted
- List

1. Numbered
2. List

**Bold** and _Italic_ and `Code` text

[Link](url) and ![Image](src)
```

For more details see [GitHub Flavored Markdown](https://guides.github.com/features/mastering-markdown/).

### Jekyll Themes

Your Pages site will use the layout and styles from the Jekyll theme you have selected in your [repository settings](https://github.com/GustavoVale/merge-conflict-resolution-analysis/settings/pages). The name of this theme is saved in the Jekyll `_config.yml` configuration file.

### Support or Contact

Having trouble with Pages? Check out our [documentation](https://docs.github.com/categories/github-pages-basics/) or [contact support](https://support.github.com/contact) and we’ll help you sort it out.
