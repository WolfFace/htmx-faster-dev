# HTMXFaster / [htmx-faster.org](https://htmx-faster.org)

Website built with Clojure, HTMX, and Alpine.js. \
Remake of NextFaster, a highly performant e-commerce template using Next.js and AI generated content by [@ethanniser](https://x.com/ethanniser), [@RhysSullivan](https://x.com/RhysSullivan) and [@armans-code](https://x.com/ksw_arman)

**Check original [NextFaster](https://github.com/ethanniser/NextFaster) repo**

### Motivation
This projects demonstrates how beautiful HTMX is. This version has 2 times smaller codebase, has not aggressive preloading, has less JS on frontend.

### Costs
- $5 / month cheapest Linode 1 GB VPS
- Cloudflare Free Tier
- Bunny CDN for images that costs cents
- $6 / year for domain name

### Deployment

- Clone this repo
- Create `.env`. Refer to `.env.sample`
- Run `./scripts/download-data.sh`. It will download `./init-scripts/data.sql`
- Place your `private.key.pem` and `domain.cert.pem` to `./nginx/ssl` directory
- Run `docker compose -f docker-compose.prod.yml up -d`. It will download product images at first time.

### Local development

- Clone this repo
- Create `.env`. Refer to `.env.sample`
- Run `./scripts/download-data.sh`. It will download `./init-scripts/data.sql`
- Run `docker compose -f docker-compose.dev.yml up`. It will download product images at first time.
- Connect to nrepl by port 8888 and have fun!

### Why HTMX?

- No build tools
- Less JS on your page
- Easy to make an SPA using `hx-boost` attribute on page's `<body>`
- No duplicated logic between Backend/Frontend
- Thin client

### Does anyone use HTMX in production?

- [Komiku](https://komiku.id/) Mango reading website. You can read your favorite manga even if you turn off JS if you like
- [Futbin](https://www.futbin.com/) Websites is build with HTMX but uses React.js when it has sense like Squad Builder feature
- [Rumble](https://rumble.com/) Conservative and far-right Youtube alternative. 
- [kasta.ua](https://kasta.ua) One of the largest e-commerce website in Ukraine. It's not HTMX, but [Twinspark.js](https://twinspark.js.org/) which has the same philosophy
- and more...

## License

Refer to `LICENSE`
