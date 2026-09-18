# Heritage Grade Documentation Staging (Stable)
FROM ruby:3.2-slim

RUN apt-get update && apt-get install -y --no-install-recommends build-essential git && rm -rf /var/lib/apt/lists/*

WORKDIR /srv/jekyll

# Install dependencies directly in the workspace
COPY docs/Gemfile* .
RUN bundle install

# Copy source
COPY docs/ .

EXPOSE 4000

# Run Jekyll from the workspace root
CMD ["bundle", "exec", "jekyll", "serve", "--host", "0.0.0.0", "--incremental", "--force_polling", "--source", ".", "--destination", "_site"]
