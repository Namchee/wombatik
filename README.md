**USEFUL LINKS**
- [Wombatik](#wombatik)
- [Pixel Indicator Technique](#pixel-indicator-technique)
- [Batasan](#batasan)
- [Requirements](#requirements)
- [Special Thanks](#special-thanks)


# Wombatik

> Dibuat untuk memperingati Hari Batik yang jatuh pada tanggal 2 Oktober

Wombatik adalah sebuah aplikasi kecil yang bertujuan untuk mempertahankan keaslian batik Indonesia. Aplikasi ini menggunakan teknik [steganografi](https://en.wikipedia.org/wiki/Steganography) (tepatnya, [_pixel indicator technique_](#pixel-indicator-technique)) untuk menyisipkan pesan tanda kepemilikan (selanjutnya akan disebut *watermark*) pada gambar batik Indonesia. Sehingga kedepannya, masalah asal-muasal suatu motif batik dapat diselesaikan hanya dengan melihat keberadaan *watermark* pada gambar motif batik tersebut.

Selain hal-hal diatas, _watermark_ akan menyelesaikan masalah-masalah klasik dalam dunia visual, seperti _content authentication_ dan _integrity_.

## Pixel Indicator Technique

> Supaya anda dapat memahami topik ini, ada baiknya bila anda memiliki pengetahuan dasar mengenai [_bit numbering_](https://en.wikipedia.org/wiki/Bit_numbering), [representasi gambar digital](<https://en.wikipedia.org/wiki/Channel_(digital_image)>),dan representasi [karakter _ASCII_](https://en.wikipedia.org/wiki/ASCII) terlebih dahulu.

Pada dasarnya, _pixel indicator technique_ merupakan teknik yang memanfaatkan _least significant bit_ untuk menyembunyikan _watermark_ pada gambar target (selanjutnya, akan disebut _carrier_) yang memiliki _channel_ RGB. Penjelasan mengenai teknik ini dapat dilihat melalui _flowchart_ dibawah ini.

<b><p align="center">Tabel Penentuan pemilihan <i>channel</i></p></b>

| Tipe panjang pesan | Channel Indikator | Channel 1 & 2 apabila jumlah parity bit ganjil | Channel 1 & 2 apabila jumlah parity bit genap |
|:------------------:|:-----------------:|:----------------------------------------------:|:---------------------------------------------:|
|   Bilangan Genap   |       Merah       |                   Hijau, Biru                  |                  Biru, Hijau                  |
|   Bilangan Ganjil  |        Biru       |                  Merah, Hijau                  |                  Hijau, Merah                 |
|   Bukan Keduanya   |       Hijau       |                   Merah, Biru                  |                  Biru, Merah                  |

<b><p align="center">Menyisipkan <i>watermark</i></p></b>

<p align="center"><img src="https://image.ibb.co/eVio3A/Untitled-Diagram-2.png" title="Algoritma penyisipan watermark" alt="algoritma_penyisipan" /></p>

<b><p align="center">Mengambil <i>watermark</i></p></b>

<p align="center"><img src="https://pictr.com/images/2018/10/20/01Fz6B.png" title="Algoritma pengambilan watermark" alt="algoritma_pengambilan" /></p>

## Batasan

- _semi-fragile_ (perubahan yang besar pada gambar akan merusak gambar), sehingga hanya bisa menghasilkan gambar dengan _format_ `bmp`
- Hanya mampu menyisipkan _watermark_ pada gambar dengan _channel_ RGB saja (bagaimana dengan CMYK, atau _grayscale_?)
- Hanya mampu menyisipkan _watermark_ maksimal 255 karakter.

## Requirements

- Java, minimal versi 8
- Gambar dan _watermark_ tentunya

## Changelog

- v1.1, menambah batas watermark menjadi 64 KB, kemudahan _upload_ watermark melalui _file_, dan kemampuan untuk membuat _log_
- v1.0, initial release

## Special Thanks

- Mariskha Tri Adithia
- Kristopher David Harjono
- Samuel Lusandi
- Gunawan Christanto
