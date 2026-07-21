from __future__ import annotations

from typing import Final

__all__ = [
    "BANKS", "OJK_PINJOL_ILEGAL", "SCAM_KEYWORDS",
    "JUDOL_KEYWORDS", "JUDOL_DOMAINS", "URGENCY_MARKERS",
    "URL_SHORTENERS",
]

BANKS: Final[dict[str, str]] = {
    "bca": "BCA", "mandiri": "Mandiri", "bri": "BRI",
    "bni": "BNI", "bsi": "BSI", "cimb": "CIMB Niaga",
    "danamon": "Danamon", "permata": "Permata",
    "maybank": "Maybank", "panin": "Panin",
    "uob": "UOB", "ocbc": "OCBC NISP",
    "btn": "BTN", "bukopin": "Bukopin",
    "jenius": "Jenius", "digibank": "Digibank",
    "blu": "Blu by BCA Digital",
}

OJK_PINJOL_ILEGAL: Final[list[str]] = [
    "danacepat", "uangteman", "rupiahcepat", "danaaku",
    "pinjamonline", "cepatkaya", "uanginstan", "danasyariah",
    "pinjamsekarang", "danatunai", "uangcepat", "cepatdana",
    "danamudah", "akuaku", "rupiahcepatid", "danacepatid",
    "temanuang", "kawanmoney", "cepatuang",
]

SCAM_KEYWORDS: Final[list[str]] = [
    "transfer sekarang", "batas waktu", "hadiah", "terpilih",
    "klik link", "verifikasi akun", "undian", "pemenang",
    "ubah jadi", "gandakan", "modal kecil", "return besar",
    "jaminan", "bebas agunan", "cair sekarang", "tanpa BI checking",
    "tanpa riba", "halal", "dana darurat", "dana cepat",
]

JUDOL_KEYWORDS: Final[list[str]] = [
    "slot", "togel", "judi", "casino", "maxwin", "gacor",
    "deposit", "withdraw", "jackpot", "bonus new member",
    "situs", "bandar", "agen judi", "daftar slot",
    "slot online", "judi online", "game slot", "slot gacor",
    "modal kecil", "hasil besar", "kemenangan", "member baru",
    "free spin", "no deposit", "turnover", "rollingan",
    "rungkat", "rungkad", "totop4", "situs toto", "bonanza",
    "pragmatic", "pg soft", "sweet bonanza", "gates of olympus",
    "starlight princess", "zeus", "kakek zeus",
]

JUDOL_DOMAINS: Final[list[str]] = [
    "slot", "togel", "casino", "judionline", "judol",
    "totop4", "bonanza", "pragmatic",
]

URGENCY_MARKERS: Final[list[str]] = [
    "segera", "hari ini", "jam lagi", "batas", "terakhir", "jangan lewatkan",
]

URL_SHORTENERS: Final[set[str]] = {
    "bit.ly", "tinyurl", "shorturl", "s.id", "rb.gy", "tiny.cc",
}
