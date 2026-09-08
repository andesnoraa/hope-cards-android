#!/bin/bash
set -euo pipefail

store_root="assets/store"
video_root="$store_root/videos"
promo_music="${HOPE_CARDS_PROMO_MUSIC:-$store_root/source/open-hands-glow.mp3}"

command -v ffmpeg >/dev/null 2>&1 || { echo "ffmpeg is required" >&2; exit 1; }
[ -f "$promo_music" ] || { echo "Missing promo music: $promo_music" >&2; exit 1; }

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
      -f lavfi -i "color=c=0xF1F4F8:s=1920x1080" -i "$first" -i "$second" \
      -filter_complex "[0:v]format=rgba[bg];[1:v]scale=-2:1000[first];[2:v]scale=-2:1000[second];[bg][first]overlay=360:(H-h)/2[tmp];[tmp][second]overlay=W-w-360:(H-h)/2" \
      -frames:v 1 "$output"
  }

  pair_scene "$phone/01-draw-a-card.png" "$phone/02-read-scripture.png" "$scene_dir/cards.png"
  pair_scene "$phone/03-daily-hope.png" "$phone/04-save-favorites.png" "$scene_dir/daily.png"
  pair_scene "$phone/05-journal-notes.png" "$phone/06-choose-a-theme.png" "$scene_dir/journal.png"

  ffmpeg -hide_banner -loglevel error -y \
    -loop 1 -t 7 -i "$hero" \
    -loop 1 -t 7 -i "$scene_dir/cards.png" \
    -loop 1 -t 7 -i "$scene_dir/daily.png" \
    -loop 1 -t 7 -i "$scene_dir/journal.png" \
    -loop 1 -t 7 -i "$hero" \
    -i "$promo_music" \
    -filter_complex "[0:v]zoompan=z='min(zoom+0.000075,1.016)':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=210:s=1920x1080:fps=30,trim=duration=7,setpts=PTS-STARTPTS[v0];[1:v]zoompan=z='min(zoom+0.000075,1.016)':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=210:s=1920x1080:fps=30,trim=duration=7,setpts=PTS-STARTPTS[v1];[2:v]zoompan=z='min(zoom+0.000075,1.016)':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=210:s=1920x1080:fps=30,trim=duration=7,setpts=PTS-STARTPTS[v2];[3:v]zoompan=z='min(zoom+0.000075,1.016)':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=210:s=1920x1080:fps=30,trim=duration=7,setpts=PTS-STARTPTS[v3];[4:v]zoompan=z='min(zoom+0.000075,1.016)':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=210:s=1920x1080:fps=30,trim=duration=7,setpts=PTS-STARTPTS[v4];[v0][v1]xfade=transition=fade:duration=0.75:offset=6.25[x1];[x1][v2]xfade=transition=fade:duration=0.75:offset=12.5[x2];[x2][v3]xfade=transition=fade:duration=0.75:offset=18.75[x3];[x3][v4]xfade=transition=fade:duration=0.75:offset=25,format=yuv420p[video];[5:a]atrim=0:32,asetpts=PTS-STARTPTS,volume=0.82,afade=t=in:st=0:d=1,afade=t=out:st=30:d=2[audio]" \
    -map "[video]" -map "[audio]" -t 32 \
    -c:v libx264 -preset slow -crf 18 -profile:v high -level 4.1 -pix_fmt yuv420p \
    -c:a aac -b:a 256k -ar 48000 -movflags +faststart \
    "$destination/hope-cards-promo-$locale.mp4"

  title=$(tr -d '\n\r\t' < "$listing/video-title.txt")
  printf '%s\t%s\t%s\n' "$locale" "$title" "$destination/hope-cards-promo-$locale.mp4" >> "$manifest"
  rm -rf "$scene_dir"
  trap - EXIT
done

echo "Generated localized Play Store promo videos in $video_root."
