# Pacman Browser Build

This repository keeps the original Java/Swing sources and adds a browser build in `web/`.

## Run locally

```bash
python server.py --host 0.0.0.0 --port 8080
```

Open:

```text
http://localhost:8080/
```

On a VM, use the VM public IP or DNS name:

```text
http://<vm-ip>:8080/
```

Make sure the VM firewall or cloud security group allows inbound TCP traffic on the selected port.

## Controls

- Arrow keys or WASD: move
- `Space` or `P`: pause
- `R`: restart
- `M`: mute or unmute

Mobile browsers can swipe on the maze or use the on-screen direction pad.

## Files

- `web/index.html`: browser entry point
- `web/styles.css`: page layout
- `web/game.js`: Canvas game loop and Pacman logic
- `web/assets/`: browser-ready copies of the sprite, maze, and sound assets
- `server.py`: no-dependency static server for local or VM hosting

## Optional systemd service

Create `/etc/systemd/system/pacman.service` on the VM:

```ini
[Unit]
Description=Pacman browser game
After=network.target

[Service]
WorkingDirectory=/opt/pacman
ExecStart=/usr/bin/python3 /opt/pacman/server.py --host 0.0.0.0 --port 8080
Restart=always
User=www-data

[Install]
WantedBy=multi-user.target
```

Then run:

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now pacman
sudo systemctl status pacman
```
