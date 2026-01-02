FROM php:8.2-cli

# Install dependencies sistem yang dibutuhkan Laravel dan Composer
RUN apt-get update && apt-get install -y \
    git \
    curl \
    libpng-dev \
    libonig-dev \
    libxml2-dev \
    zip \
    unzip \
    default-mysql-client

# Install ekstensi PHP yang umum untuk Laravel
RUN docker-php-ext-install pdo_mysql mbstring exif pcntl bcmath gd

# Install Composer (ambil dari image official composer)
COPY --from=composer:latest /usr/bin/composer /usr/bin/composer

# Set working directory di dalam container
WORKDIR /var/www

# Expose port yang kamu gunakan di konfigurasi
EXPOSE 60600

# Entrypoint default php image
CMD ["bash", "-lc", "if [ -f artisan ]; then composer install --no-interaction --prefer-dist --optimize-autoloader; php artisan config:clear || true; php artisan cache:clear || true; php artisan key:generate --force || true; php artisan serve --host=0.0.0.0 --port=60600; else echo 'File artisan tidak ditemukan. Pastikan volume sudah termount ke /var/www'; sleep 3600; fi"]