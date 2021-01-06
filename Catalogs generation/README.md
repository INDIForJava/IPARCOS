# Star and DSO catalog generation

To generate the catalogs, query the NGC, Barnard and Common Star catalogs from Vizier.

## [Common star catalog](https://vizier.u-strasbg.fr/viz-bin/VizieR?-source=IV/22)

- Options:
  - Limit: 9999
  - J2000
  - No sort
  - Decimal
  - Tab-separated values
- Simple constrains:
  - Vmag <4
  - SAO
  - HD
  - BFno
  - name

Remove comments and empty lines (at the end of the file, too) from the output file and save it as `stars.tsv` in the raw resources.

## [NGC catalog](https://vizier.u-strasbg.fr/viz-bin/VizieR-3?-source=VII/118/ngc2000)

## [Barnard catalog](https://vizier.u-strasbg.fr/viz-bin/VizieR-3?-source=VII/220A/barnard)
