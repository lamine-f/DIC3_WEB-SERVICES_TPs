## Makefile — Rapport academique FINAL_PROJECT

MAIN      := main
DIAGRAMS  := $(wildcard diagrams/*.puml)
DIAG_PNG  := $(patsubst diagrams/%.puml,figures/plantuml/%.png,$(DIAGRAMS))
TEX_FILES := $(wildcard chapters/*.tex) $(MAIN).tex preamble.tex meta.tex

.PHONY: all diagrams slides slides-serve clean distclean spell

all: diagrams $(MAIN).pdf

## Compilation du PDF (3 passes pour resoudre refs + biblio)
$(MAIN).pdf: $(TEX_FILES) $(DIAG_PNG) bib/references.bib
	pdflatex -interaction=nonstopmode -halt-on-error $(MAIN).tex
	-bibtex $(MAIN)
	pdflatex -interaction=nonstopmode -halt-on-error $(MAIN).tex
	pdflatex -interaction=nonstopmode -halt-on-error $(MAIN).tex
	@echo "==> PDF genere : $(MAIN).pdf"

## Generation des diagrammes PlantUML
diagrams: $(DIAG_PNG)

figures/plantuml/%.png: diagrams/%.puml
	@mkdir -p figures/plantuml
	plantuml -tpng -o $(abspath figures/plantuml) $<

## Verification orthographique d'un chapitre
## Usage : make spell CHAP=chapters/02-conception.tex
spell:
	@if [ -z "$(CHAP)" ]; then echo "Usage: make spell CHAP=chapters/XX.tex"; exit 1; fi
	aspell --lang=fr-FR --mode=tex -c $(CHAP)

## Compilation de la presentation Marp -> slides.pdf
## Pre-requis : npm install -g @marp-team/marp-cli
slides: $(DIAG_PNG) slides.md
	marp slides.md --pdf --allow-local-files
	@echo "==> Slides generees : slides.pdf"

## Visualisation interactive de la presentation (http://localhost:8080)
slides-serve:
	marp slides.md --server --allow-local-files

## Nettoyage des fichiers intermediaires LaTeX
clean:
	rm -f $(MAIN).aux $(MAIN).log $(MAIN).toc $(MAIN).lof $(MAIN).lot \
	      $(MAIN).fls $(MAIN).fdb_latexmk $(MAIN).out $(MAIN).bbl \
	      $(MAIN).blg $(MAIN).bcf $(MAIN).run.xml $(MAIN).synctex.gz
	rm -f chapters/*.aux

## Nettoyage complet (PDF + PNG generes)
distclean: clean
	rm -f $(MAIN).pdf slides.pdf
	rm -rf figures/plantuml
