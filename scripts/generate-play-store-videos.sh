#!/bin/bash
set -euo pipefail

store_root="assets/store"
background="$store_root/source/feature-background.png"
video_root="$store_root/videos"

command -v ffmpeg >/dev/null 2>&1 || { echo "ffmpeg is required" >&2; exit 1; }
[ -f "$background" ] || { echo "Missing $background" >&2; exit 1; }

mkdir -p "$video_root"
manifest="$video_root/upload-manifest.tsv"
printf 'locale\ttitle\tfile\n' > "$manifest"

for listing in "$store_root"/listings/*; do
  [ -d "$listing" ] || continue
  locale=$(basename "$listing")
  phone="$store_root/screenshots/phone/$locale"
  destination="$video_root/$locale"
  mkdir -p "$destination"
  scene_dir=$(mktemp -d)
  trap 'rm -rf "$scene_dir"' EXIT

  hero="$destination/hero.png"
  [ -f "$hero" ] || { echo "Missing generated video hero: $hero" >&2; exit 1; }

  pair_scene() {
    local first="$1"
    local second="$2"
    local output="$3"
    ffmpeg -hide_banner -loglevel error -y \
      -i "$background" -i "$first" -i "$second" \
      -filter_complex "[0:v]scale=1920:1080,drawbox=color=0x071a33@0.52:t=fill[bg];[1:v]scale=-2:1000[first];[2:v]scale=-2:1000[second];[bg][first]overlay=360:(H-h)/2[tmp];[tmp][second]overlay=W-w-360:(H-h)/2" \
      -frames:v 1 "$output"
  }

  pair_scene "$phone/01-draw-a-card.png" "$phone/02-read-scripture.png" "$scene_dir/cards.png"
  pair_scene "$phone/03-daily-hope.png" "$phone/04-save-favorites.png" "$scene_dir/daily.png"
  pair_scene "$phone/05-journal-notes.png" "$phone/06-choose-a-theme.png" "$scene_dir/journal.png"

  ffmpeg -hide_banner -loglevel error -y \
    -loop 1 -t 5 -i "$hero" \
    -loop 1 -t 5 -i "$scene_dir/cards.png" \
    -loop 1 -t 5 -i "$scene_dir/daily.png" \
    -loop 1 -t 5 -i "$scene_dir/journal.png" \
    -loop 1 -t 5 -i "$hero" \
    -f lavfi -i "aevalsrc=0.060*sin(2*PI*220*t)+0.042*sin(2*PI*329.63*t)+0.024*sin(2*PI*440*t):s=48000:d=21.8" \
    -filter_complex "[0:v]fps=30,format=yuv420p,setpts=PTS-STARTPTS[v0];[1:v]fps=30,format=yuv420p,setpts=PTS-STARTPTS[v1];[2:v]fps=30,format=yuv420p,setpts=PTS-STARTPTS[v2];[3:v]fps=30,format=yuv420p,setpts=PTS-STARTPTS[v3];[4:v]fps=30,format=yuv420p,setpts=PTS-STARTPTS[v4];[v0][v1]xfade=transition=fade:duration=0.8:offset=4.2[x1];[x1][v2]xfade=transition=fade:duration=0.8:offset=8.4[x2];[x2][v3]xfade=transition=fade:duration=0.8:offset=12.6[x3];[x3][v4]xfade=transition=fade:duration=0.8:offset=16.8[video];[5:a]lowpass=f=1100,aecho=0.8:0.88:70:0.22,afade=t=in:st=0:d=1.8,afade=t=out:st=19.3:d=2.5[audio]" \
    -map "[video]" -map "[audio]" -t 21.8 \
    -c:v libx264 -preset slow -crf 18 -profile:v high -level 4.1 -pix_fmt yuv420p \
    -c:a aac -b:a 160k -movflags +faststart \
    "$destination/hope-cards-promo-$locale.mp4"

  title=$(tr -d '\n\r\t' < "$listing/video-title.txt")
  printf '%s\t%s\t%s\n' "$locale" "$title" "$destination/hope-cards-promo-$locale.mp4" >> "$manifest"
  rm -rf "$scene_dir"
  trap - EXIT
done

echo "Generated localized Play Store promo videos in $video_root."
