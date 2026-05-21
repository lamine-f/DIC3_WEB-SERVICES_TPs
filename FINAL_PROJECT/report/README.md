# Rapport de projet — FINAL_PROJECT

Rapport LaTeX documentant le projet `FINAL_PROJECT` (chatroom distribuée SOAP + REST polyglotte). Format allégé de rapport de projet de fin de semestre (~20 pages), structuré en préambule + 3 chapitres techniques.

## Compilation

```bash
cd FINAL_PROJECT/report
make             # rapport : diagrammes PlantUML + main.pdf
make diagrams    # regenere uniquement les .png depuis les .puml
make slides      # presentation Marp -> slides.pdf
make slides-serve # apercu interactif sur http://localhost:8080
make clean       # supprime les fichiers intermediaires LaTeX
make distclean   # supprime aussi les PDF et les PNG
```

Sorties : `main.pdf` (rapport) et `slides.pdf` (presentation) a la racine de `report/`.

## Pre-requis

- `texlive-latex-base`, `texlive-latex-recommended`, `texlive-latex-extra`
- `texlive-lang-french` (pour Babel francais)
- `texlive-fonts-recommended`, `texlive-fonts-extra`
- `bibtex` (fourni avec `texlive-binaries`)
- `plantuml` (testé avec 1.2026.1)

Installation rapide sur Debian/Ubuntu :
```bash
sudo apt install texlive-latex-base texlive-latex-recommended texlive-latex-extra \
                 texlive-lang-french texlive-fonts-recommended plantuml
```

Pour la presentation Marp :
```bash
npm install -g @marp-team/marp-cli
```

## Structure

```
report/
├── CLAUDE.md                    # Instructions de redaction (ton + contexte projet)
├── README.md                    # Ce fichier
├── Makefile                     # Construction PDF + slides
├── main.tex                     # Document principal du rapport
├── preamble.tex                 # Packages et commandes custom
├── meta.tex                     # Auteur, titre, ecole
├── slides.md                    # Presentation Marp (18 slides sobres)
├── chapters/                    # 1 préambule + 3 chapitres en .tex
├── diagrams/                    # 7 sources PlantUML
├── figures/
│   ├── plantuml/                # PNGs generes
│   ├── postman/                 # Captures Postman (a fournir)
│   ├── soapui/                  # Captures SoapUI (a fournir)
│   ├── traces/                  # Extraits XML/JSON
│   └── captures-attendues.md    # Liste des captures a prendre
└── bib/references.bib           # Bibliographie
```

## Verification orthographique

```bash
make spell CHAP=chapters/01-conception.tex
```

## Pour rediger un chapitre

Lire `CLAUDE.md` au préalable (ton académique strict, vocabulaire varié, anti-patterns). Ce document est consulte automatiquement par Claude lors de la rédaction.
