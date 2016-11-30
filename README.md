# Joulukalenteri 2016

Simple ClojureScript application to represent my
daughters Joulukalenteri for christmas 2015.

Running app is at http://millan-joulukalenteri.fi

# Development

```bash
lein dev
```

# Build

```bash
lein dist
```

# Deployment

```bash
lein dist
./update-github-pages.sh
git add ./docs
git commit -m "..."
git push
```

# Thanks

Special thanks to Joshua Miller's blog 
http://increasinglyfunctional.com/2013/12/08/point-polygon-clojure/

# License

## Artistic content

All images Copyright © 2016 Milla Länsiö and Titta Länsiö

The "Joulukalenteri 2016" images by Milla and Titta Länsiö are licensed
under a [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License](http://creativecommons.org/licenses/by-nc-sa/4.0/).

## Code

All other parts Copyright © 2016 Jarppe Länsiö

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
