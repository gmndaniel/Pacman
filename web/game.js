(() => {
  "use strict";

  const canvas = document.getElementById("game");
  const ctx = canvas.getContext("2d");
  const statusEl = document.getElementById("status");
  const startButton = document.getElementById("startButton");
  const pauseButton = document.getElementById("pauseButton");
  const muteButton = document.getElementById("muteButton");
  const restartButton = document.getElementById("restartButton");

  const CANVAS_WIDTH = 550;
  const MAZE_HEIGHT = 560;
  const CANVAS_HEIGHT = 600;
  const FIELD_WIDTH = 30;
  const FIELD_HEIGHT = 31;
  const VIRT_BORDERS = 2;
  const W_COEFF = CANVAS_WIDTH / (FIELD_WIDTH - VIRT_BORDERS);
  const H_COEFF = MAZE_HEIGHT / FIELD_HEIGHT;
  const SPRITE_SIZE = 32;
  const CENTER_EPSILON = 0.045;
  const TURN_SNAP_DISTANCE = 0.24;
  const INTRO_READY_SECONDS = 4.25;

  const TYPE = {
    WALL: 0,
    DOT: 1,
    EMPTY: 2,
    GHOST_HOUSE: 3,
    SUPER: 4,
    L_PORTAL: 5,
    R_PORTAL: 6,
  };

  const DIRECTIONS = {
    up: { name: "up", dr: -1, dc: 0 },
    down: { name: "down", dr: 1, dc: 0 },
    left: { name: "left", dr: 0, dc: -1 },
    right: { name: "right", dr: 0, dc: 1 },
  };
  const DIR_LIST = [DIRECTIONS.up, DIRECTIONS.left, DIRECTIONS.down, DIRECTIONS.right];
  const OPPOSITE = { up: "down", down: "up", left: "right", right: "left" };

  const FIELD_TEMPLATE = [
    [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0],
    [0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0],
    [0, 0, 4, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 4, 0, 0],
    [0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0],
    [0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0],
    [0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0],
    [0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0],
    [0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 3, 0, 0, 0, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 2, 2, 0, 0, 0, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0],
    [5, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 0, 0, 2, 2, 2, 2, 0, 0, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 6],
    [0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0],
    [0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0],
    [0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0],
    [0, 0, 4, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 4, 0, 0],
    [0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0],
    [0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0],
    [0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0],
    [0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0],
    [0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0],
    [0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
  ];

  const MODE_SCHEDULE = [
    { mode: "scatter", seconds: 7 },
    { mode: "chase", seconds: 20 },
    { mode: "scatter", seconds: 7 },
    { mode: "chase", seconds: 20 },
    { mode: "scatter", seconds: 5 },
    { mode: "chase", seconds: 20 },
    { mode: "scatter", seconds: 5 },
    { mode: "chase", seconds: Infinity },
  ];

  const PACMAN_CHEW_FRAME_SECONDS = 0.045;
  const PACMAN_CHEW_DURATION = 0.24;
  const PACMAN_CHEW_FRAMES = {
    right: [[0, 0], [1, 0], [2, 0], [1, 0]],
    left: [[0, 1], [1, 1], [2, 0], [1, 1]],
    up: [[0, 2], [1, 2], [2, 0], [1, 2]],
    down: [[0, 3], [1, 3], [2, 0], [1, 3]],
  };

  const PACMAN_STATIC_FRAMES = {
    right: [1, 0],
    left: [1, 1],
    up: [1, 2],
    down: [1, 3],
  };

  const PACMAN_DEATH_FRAMES = [
    [2, 0], [3, 0], [4, 0], [5, 0], [6, 0], [7, 0], [8, 0],
    [9, 0], [10, 0], [11, 0], [12, 0], [13, 0], [13, 1],
  ];

  const GHOST_ROWS = {
    blinky: 4,
    pinky: 5,
    inky: 6,
    clyde: 7,
  };

  const GHOST_TARGETS = {
    blinky: { row: 1, col: 24 },
    pinky: { row: 1, col: 4 },
    inky: { row: 29, col: 22 },
    clyde: { row: 29, col: 8 },
  };

  const assets = {};
  const audio = {};
  let audioContext = null;
  let spriteSheet = null;
  let lastTime = performance.now();
  let statusText = "";

  const state = {
    started: false,
    paused: false,
    status: "loading",
    statusTimer: 0,
    level: 1,
    score: 0,
    lives: 3,
    grid: [],
    remainingPellets: 0,
    pacman: null,
    ghosts: [],
    requestedDir: DIRECTIONS.left,
    modeIndex: 0,
    modeTime: 0,
    frightTime: 0,
    ghostEatScore: 200,
    muted: false,
    audioReady: false,
    sirenActive: false,
    superActive: false,
    deathStarted: false,
  };

  class Sound {
    constructor(src, loop = false) {
      this.audio = new Audio(src);
      this.audio.preload = "auto";
      this.audio.loop = loop;
    }

    play(reset = true) {
      if (state.muted || !state.audioReady) {
        return;
      }
      if (reset) {
        this.audio.currentTime = 0;
      }
      const playPromise = this.audio.play();
      if (playPromise) {
        playPromise.catch(() => undefined);
      }
    }

    stop() {
      this.audio.pause();
      this.audio.currentTime = 0;
    }

    load() {
      this.audio.load();
    }
  }

  class OneShotSample {
    constructor(src, options = {}) {
      this.src = src;
      this.buffer = null;
      this.context = null;
      this.loadPromise = null;
      this.offsets = options.offsets || [0];
      this.duration = options.duration || 0.1;
      this.minInterval = options.minInterval || 0.06;
      this.gain = options.gain || 0.85;
      this.fadeOut = options.fadeOut || 0.02;
      this.offsetIndex = 0;
      this.lastStartedAt = -Infinity;
    }

    load(context) {
      if (!context) {
        return Promise.resolve();
      }

      this.context = context;

      if (this.buffer || this.loadPromise) {
        return this.loadPromise;
      }

      this.loadPromise = fetch(this.src)
        .then((response) => response.arrayBuffer())
        .then((data) => context.decodeAudioData(data))
        .then((buffer) => {
          this.buffer = buffer;
        })
        .catch(() => {
          this.loadPromise = null;
        });

      return this.loadPromise;
    }

    play() {
      if (state.muted || !state.audioReady || !this.context || !this.buffer) {
        return;
      }

      if (this.context.state === "suspended") {
        this.context.resume();
      }

      const now = this.context.currentTime;
      if (now - this.lastStartedAt < this.minInterval) {
        return;
      }

      const source = this.context.createBufferSource();
      const gainNode = this.context.createGain();
      const offset = this.offsets[this.offsetIndex];
      const fadeStart = Math.max(now, now + this.duration - this.fadeOut);

      source.buffer = this.buffer;
      gainNode.gain.setValueAtTime(this.gain, now);
      gainNode.gain.setValueAtTime(this.gain, fadeStart);
      gainNode.gain.linearRampToValueAtTime(0.001, now + this.duration);
      source.connect(gainNode).connect(this.context.destination);
      source.start(now, offset, this.duration);

      this.offsetIndex = (this.offsetIndex + 1) % this.offsets.length;
      this.lastStartedAt = now;
    }

    stop() {
      this.lastStartedAt = -Infinity;
    }
  }

  class LoopingSample {
    constructor(src, options = {}) {
      this.src = src;
      this.buffer = null;
      this.context = null;
      this.loadPromise = null;
      this.source = null;
      this.gainNode = null;
      this.gain = options.gain || 0.55;
      this.loopStart = options.loopStart || 0;
      this.loopEnd = options.loopEnd || null;
      this.fade = options.fade || 0.035;
    }

    load(context) {
      if (!context) {
        return Promise.resolve();
      }

      this.context = context;

      if (this.buffer || this.loadPromise) {
        return this.loadPromise;
      }

      this.loadPromise = fetch(this.src)
        .then((response) => response.arrayBuffer())
        .then((data) => context.decodeAudioData(data))
        .then((buffer) => {
          this.buffer = buffer;
        })
        .catch(() => {
          this.loadPromise = null;
        });

      return this.loadPromise;
    }

    play(reset = true) {
      if (state.muted || !state.audioReady || !this.context || !this.buffer) {
        return;
      }

      if (this.context.state === "suspended") {
        this.context.resume();
      }

      if (this.source && !reset) {
        return;
      }

      this.stop(0);

      const now = this.context.currentTime;
      const source = this.context.createBufferSource();
      const gainNode = this.context.createGain();

      source.buffer = this.buffer;
      source.loop = true;
      source.loopStart = this.loopStart;
      source.loopEnd = this.loopEnd || this.buffer.duration;
      gainNode.gain.setValueAtTime(0.001, now);
      gainNode.gain.linearRampToValueAtTime(this.gain, now + this.fade);
      source.connect(gainNode).connect(this.context.destination);
      source.start(now, this.loopStart);

      this.source = source;
      this.gainNode = gainNode;
      source.onended = () => {
        if (this.source === source) {
          this.source = null;
          this.gainNode = null;
        }
      };
    }

    stop(fade = this.fade) {
      if (!this.source || !this.context || !this.gainNode) {
        return;
      }

      const source = this.source;
      const gainNode = this.gainNode;
      const now = this.context.currentTime;

      gainNode.gain.cancelScheduledValues(now);
      gainNode.gain.setValueAtTime(Math.max(gainNode.gain.value, 0.001), now);
      gainNode.gain.linearRampToValueAtTime(0.001, now + fade);
      source.stop(now + fade + 0.01);

      this.source = null;
      this.gainNode = null;
    }
  }

  function loadImage(src) {
    return new Promise((resolve, reject) => {
      const image = new Image();
      image.onload = () => resolve(image);
      image.onerror = () => reject(new Error(`Could not load ${src}`));
      image.src = src;
    });
  }

  function loadAssets() {
    return Promise.all([
      loadImage("./assets/images/maze_small.png").then((image) => {
        assets.maze = image;
      }),
      loadImage("./assets/images/sprites.png").then((image) => {
        spriteSheet = transparentSpriteSheet(image);
      }),
      loadImage("./assets/images/ready.png").then((image) => {
        assets.ready = image;
      }),
      loadImage("./assets/images/game_over.png").then((image) => {
        assets.gameOver = image;
      }),
    ]);
  }

  function configureAudio() {
    audio.beginning = new Sound("./assets/sounds/pacman_beginning.wav");
    audio.chomp = new OneShotSample("./assets/sounds/pacman_chomp.wav", {
      offsets: [0, 0.15],
      duration: 0.18,
      minInterval: 0.08,
      gain: 0.9,
      fadeOut: 0.025,
    });
    audio.death = new Sound("./assets/sounds/pacman_death.wav");
    audio.eatGhost = new Sound("./assets/sounds/pacman_eatghost.wav");
    audio.super = new LoopingSample("./assets/sounds/pacman_super.wav", {
      loopStart: 0,
      loopEnd: 0.515,
      gain: 0.48,
    });
    audio.siren = new LoopingSample("./assets/sounds/pacman_siren.wav", {
      loopStart: 0,
      loopEnd: 1.6,
      gain: 0.42,
    });
  }

  function transparentSpriteSheet(image) {
    const sheet = document.createElement("canvas");
    sheet.width = image.naturalWidth;
    sheet.height = image.naturalHeight;
    const sheetCtx = sheet.getContext("2d");
    sheetCtx.drawImage(image, 0, 0);
    const pixels = sheetCtx.getImageData(0, 0, sheet.width, sheet.height);

    for (let i = 0; i < pixels.data.length; i += 4) {
      const red = pixels.data[i];
      const green = pixels.data[i + 1];
      const blue = pixels.data[i + 2];

      if (red > 220 && green < 45 && blue > 160) {
        pixels.data[i + 3] = 0;
      }
    }

    sheetCtx.putImageData(pixels, 0, 0);
    return sheet;
  }

  function cloneGrid() {
    return FIELD_TEMPLATE.map((row) => row.slice());
  }

  function countPellets(grid) {
    let pellets = 0;
    for (const row of grid) {
      for (const type of row) {
        if (type === TYPE.DOT || type === TYPE.SUPER) {
          pellets += 1;
        }
      }
    }
    return pellets;
  }

  function makePacman() {
    return {
      kind: "pacman",
      row: 23,
      col: 15,
      startRow: 23,
      startCol: 15,
      dir: DIRECTIONS.left,
      desiredDir: DIRECTIONS.left,
      speed: 7.6,
      alive: true,
      deathElapsed: 0,
      isChewing: false,
      mouthElapsed: 0,
      chewTimeRemaining: 0,
      mouthFrameIndex: 0,
    };
  }

  function makeGhost(name, row, col, dirName, releaseAt) {
    return {
      kind: "ghost",
      name,
      row,
      col,
      startRow: row,
      startCol: col,
      dir: DIRECTIONS[dirName],
      speed: 5.7,
      baseSpeed: 5.7,
      released: releaseAt === 0,
      releaseAt,
      mode: "normal",
      previousMode: "normal",
      eaten: false,
      blink: false,
      releaseTarget: { row: 11, col: 15 },
    };
  }

  function resetGame() {
    state.started = false;
    state.paused = false;
    state.level = 1;
    state.score = 0;
    state.lives = 3;
    resetBoard();
    setRoundReady();
    setStatus("Ready");
  }

  function resetBoard() {
    state.grid = cloneGrid();
    state.remainingPellets = countPellets(state.grid);
    resetActors();
    resetMode();
  }

  function resetActors() {
    state.pacman = makePacman();
    state.ghosts = [
      makeGhost("blinky", 11, 15, "left", 0),
      makeGhost("pinky", 14, 15, "up", 4),
      makeGhost("inky", 14, 13, "right", 9),
      makeGhost("clyde", 14, 16, "left", 14),
    ];
    state.requestedDir = DIRECTIONS.left;
    state.frightTime = 0;
    state.ghostEatScore = 200;
    state.deathStarted = false;
  }

  function resetMode() {
    state.modeIndex = 0;
    state.modeTime = 0;
  }

  function setRoundReady() {
    state.status = "ready";
    state.statusTimer = INTRO_READY_SECONDS;
    state.sirenActive = false;
    state.superActive = false;
    stopLoops();
  }

  function startGame() {
    unlockAudio();

    if (state.status === "gameover") {
      resetGame();
    }

    if (!state.started) {
      state.started = true;
      state.paused = false;
      audio.beginning.play();
    }
  }

  function togglePause() {
    if (!state.started || state.status === "gameover") {
      return;
    }
    state.paused = !state.paused;
    pauseButton.textContent = state.paused ? "Resume" : "Pause";
    if (state.paused) {
      stopLoops();
    }
  }

  function toggleMute() {
    state.muted = !state.muted;
    muteButton.textContent = state.muted ? "Sound Off" : "Sound On";
    muteButton.setAttribute("aria-pressed", String(state.muted));
    if (state.muted) {
      stopAllAudio();
    }
  }

  function unlockAudio() {
    if (state.audioReady) {
      return;
    }

    const AudioContextConstructor = window.AudioContext || window.webkitAudioContext;
    if (AudioContextConstructor) {
      audioContext = new AudioContextConstructor();
      audioContext.resume();
    }

    state.audioReady = true;
    for (const sound of Object.values(audio)) {
      sound.load(audioContext);
    }
  }

  function stopLoops() {
    audio.siren.stop();
    audio.super.stop();
    state.sirenActive = false;
    state.superActive = false;
  }

  function stopAllAudio() {
    for (const sound of Object.values(audio)) {
      sound.stop();
    }
    state.sirenActive = false;
    state.superActive = false;
  }

  function updateAudioLoops() {
    if (state.muted || !state.audioReady || state.status !== "playing") {
      return;
    }

    const shouldPlaySuper = state.frightTime > 0;
    const shouldPlaySiren = !shouldPlaySuper;

    if (shouldPlaySuper && !state.superActive) {
      audio.siren.stop();
      audio.super.play(false);
      state.superActive = true;
      state.sirenActive = false;
    }

    if (shouldPlaySiren && !state.sirenActive) {
      audio.super.stop();
      audio.siren.play(false);
      state.sirenActive = true;
      state.superActive = false;
    }
  }

  function setRequestedDirection(name) {
    const direction = DIRECTIONS[name];
    if (!direction) {
      return;
    }

    startGame();
    state.requestedDir = direction;
    if (state.pacman) {
      state.pacman.desiredDir = direction;

      if (isOppositeDirection(state.pacman.dir, direction)) {
        state.pacman.dir = direction;
      }
    }

  }

  function update(dt) {
    if (!state.started || state.paused) {
      return;
    }

    if (state.status === "ready") {
      state.statusTimer -= dt;
      if (state.statusTimer <= 0) {
        state.status = "playing";
        state.statusTimer = 0;
      }
      return;
    }

    if (state.status === "dying") {
      updateDeath(dt);
      return;
    }

    if (state.status === "level-clear") {
      state.statusTimer -= dt;
      if (state.statusTimer <= 0) {
        state.level += 1;
        resetBoard();
        setRoundReady();
      }
      return;
    }

    if (state.status !== "playing") {
      return;
    }

    updateMode(dt);
    updateFrightened(dt);
    updatePacman(dt);
    eatPellets();
    updatePacmanMouth(dt);
    updateGhosts(dt);
    handleCollisions();
    updateAudioLoops();

    if (state.remainingPellets <= 0) {
      state.status = "level-clear";
      state.statusTimer = 1.8;
      stopLoops();
    }
  }

  function updateDeath(dt) {
    const pacman = state.pacman;
    pacman.deathElapsed += dt;

    if (!state.deathStarted) {
      state.deathStarted = true;
      stopLoops();
      audio.death.play();
    }

    if (pacman.deathElapsed >= 1.8) {
      state.lives -= 1;
      if (state.lives <= 0) {
        state.status = "gameover";
        state.started = false;
        stopLoops();
      } else {
        resetActors();
        setRoundReady();
        if (state.audioReady) {
          audio.beginning.play();
        }
      }
    }
  }

  function updateMode(dt) {
    const segment = MODE_SCHEDULE[state.modeIndex];
    state.modeTime += dt;

    if (state.modeTime >= segment.seconds && state.modeIndex < MODE_SCHEDULE.length - 1) {
      state.modeTime = 0;
      state.modeIndex += 1;
      for (const ghost of state.ghosts) {
        if (ghost.mode === "normal") {
          ghost.dir = reverseDirection(ghost.dir) || ghost.dir;
        }
      }
    }
  }

  function updateFrightened(dt) {
    if (state.frightTime <= 0) {
      return;
    }

    state.frightTime = Math.max(0, state.frightTime - dt);
    for (const ghost of state.ghosts) {
      if (ghost.mode === "frightened") {
        ghost.blink = state.frightTime > 0 && state.frightTime < 2;
        ghost.speed = state.frightTime > 0 ? 3.6 : ghost.baseSpeed;
        if (state.frightTime === 0) {
          ghost.mode = "normal";
          ghost.blink = false;
        }
      }
    }
  }

  function updatePacman(dt) {
    const pacman = state.pacman;

    applyBufferedTurn(pacman);

    if (isCentered(pacman)) {
      snapToCell(pacman);
      if (pacman.desiredDir && canMoveFromEntity(pacman, pacman.desiredDir, true)) {
        pacman.dir = pacman.desiredDir;
      }
      if (!canMoveFromEntity(pacman, pacman.dir, true)) {
        pacman.dir = null;
      }
    }

    moveEntity(pacman, dt, true);
  }

  function updatePacmanMouth(dt) {
    const pacman = state.pacman;
    if (!pacman) {
      return;
    }

    pacman.chewTimeRemaining = Math.max(0, pacman.chewTimeRemaining - dt);

    if (state.status !== "playing" || pacman.chewTimeRemaining <= 0) {
      pacman.isChewing = false;
      pacman.mouthElapsed = 0;
      pacman.mouthFrameIndex = 0;
      return;
    }

    pacman.isChewing = true;
    pacman.mouthElapsed += dt;
    const facing = (pacman.dir || pacman.desiredDir || DIRECTIONS.left).name;
    pacman.mouthFrameIndex =
      Math.floor(pacman.mouthElapsed / PACMAN_CHEW_FRAME_SECONDS) % PACMAN_CHEW_FRAMES[facing].length;
  }

  function startPacmanChew() {
    const pacman = state.pacman;
    if (!pacman) {
      return;
    }

    if (!pacman.isChewing) {
      pacman.mouthElapsed = 0;
      pacman.mouthFrameIndex = 0;
    }

    pacman.isChewing = true;
    pacman.chewTimeRemaining = PACMAN_CHEW_DURATION;
  }

  function applyBufferedTurn(pacman) {
    const desired = pacman.desiredDir;
    const current = pacman.dir;

    if (!desired || !current || desired.name === current.name || isOppositeDirection(current, desired)) {
      return;
    }

    const turnCell = cellAheadForTurn(pacman, current);
    if (!turnCell || !canEnterCell(turnCell.row, turnCell.col, true)) {
      return;
    }

    const nextCell = neighborCell(turnCell, desired);
    if (!nextCell || !canEnterCell(nextCell.row, nextCell.col, true)) {
      return;
    }

    if (current.dc !== 0) {
      const distanceToTurn = Math.abs(turnCell.col - pacman.col);
      if (distanceToTurn <= TURN_SNAP_DISTANCE && Math.abs(pacman.row - turnCell.row) <= CENTER_EPSILON) {
        pacman.row = turnCell.row;
        pacman.col = turnCell.col;
        pacman.dir = desired;
      }
      return;
    }

    const distanceToTurn = Math.abs(turnCell.row - pacman.row);
    if (distanceToTurn <= TURN_SNAP_DISTANCE && Math.abs(pacman.col - turnCell.col) <= CENTER_EPSILON) {
      pacman.row = turnCell.row;
      pacman.col = turnCell.col;
      pacman.dir = desired;
    }
  }

  function cellAheadForTurn(entity, direction) {
    if (direction.dc > 0) {
      return { row: Math.round(entity.row), col: Math.ceil(entity.col) };
    }
    if (direction.dc < 0) {
      return { row: Math.round(entity.row), col: Math.floor(entity.col) };
    }
    if (direction.dr > 0) {
      return { row: Math.ceil(entity.row), col: Math.round(entity.col) };
    }
    return { row: Math.floor(entity.row), col: Math.round(entity.col) };
  }

  function updateGhosts(dt) {
    const releaseClock = elapsedInRound();

    for (const ghost of state.ghosts) {
      if (!ghost.released && releaseClock >= ghost.releaseAt) {
        ghost.released = true;
        ghost.mode = "normal";
      }

      if (ghost.mode === "eaten" && distanceCells(ghost, { row: 14, col: 15 }) < 0.35) {
        ghost.mode = "normal";
        ghost.eaten = false;
        ghost.speed = ghost.baseSpeed;
      }

      if (isCentered(ghost)) {
        snapToCell(ghost);
        ghost.dir = chooseGhostDirection(ghost);
      }

      moveEntity(ghost, dt, false);
    }
  }

  function elapsedInRound() {
    const segmentsDone = MODE_SCHEDULE
      .slice(0, state.modeIndex)
      .reduce((sum, segment) => sum + segment.seconds, 0);
    return segmentsDone + state.modeTime;
  }

  function eatPellets() {
    const cell = entityCell(state.pacman);
    const type = getCellType(cell.row, cell.col);

    if (type === TYPE.DOT) {
      state.grid[cell.row][cell.col] = TYPE.EMPTY;
      state.remainingPellets -= 1;
      state.score += 10;
      startPacmanChew();
      audio.chomp.play();
    }

    if (type === TYPE.SUPER) {
      state.grid[cell.row][cell.col] = TYPE.EMPTY;
      state.remainingPellets -= 1;
      state.score += 50;
      startPacmanChew();
      frightenGhosts();
    }
  }

  function frightenGhosts() {
    state.frightTime = 6;
    state.ghostEatScore = 200;
    audio.super.play();

    for (const ghost of state.ghosts) {
      if (ghost.mode !== "eaten") {
        ghost.mode = "frightened";
        ghost.speed = 3.6;
        ghost.blink = false;
        ghost.dir = reverseDirection(ghost.dir) || ghost.dir;
      }
    }
  }

  function handleCollisions() {
    for (const ghost of state.ghosts) {
      if (distanceCells(state.pacman, ghost) > 0.72) {
        continue;
      }

      if (ghost.mode === "frightened") {
        ghost.mode = "eaten";
        ghost.eaten = true;
        ghost.blink = false;
        ghost.speed = 8.4;
        state.score += state.ghostEatScore;
        state.ghostEatScore *= 2;
        audio.eatGhost.play();
        continue;
      }

      if (ghost.mode !== "eaten") {
        state.status = "dying";
        state.pacman.dir = null;
        state.pacman.deathElapsed = 0;
        return;
      }
    }
  }

  function chooseGhostDirection(ghost) {
    const cell = entityCell(ghost);
    let candidates = legalDirections(cell, false);

    if (!ghost.released) {
      return null;
    }

    if (candidates.length === 0) {
      return null;
    }

    const canReverse = ghost.mode === "frightened" || ghost.mode === "eaten";
    if (!canReverse && ghost.dir && candidates.length > 1) {
      const reverseName = OPPOSITE[ghost.dir.name];
      candidates = candidates.filter((direction) => direction.name !== reverseName);
    }

    if (ghost.mode === "frightened") {
      return candidates[Math.floor(Math.random() * candidates.length)];
    }

    const target = ghostTarget(ghost);
    let bestDirection = candidates[0];
    let bestDistance = Infinity;

    for (const direction of candidates) {
      const next = neighborCell(cell, direction);
      if (!next) {
        continue;
      }

      const distance = pathDistance(next, target);
      if (distance < bestDistance) {
        bestDistance = distance;
        bestDirection = direction;
      }
    }

    return bestDirection;
  }

  function ghostTarget(ghost) {
    if (!ghost.released) {
      return ghost.releaseTarget;
    }

    if (ghost.mode === "eaten") {
      return { row: 14, col: 15 };
    }

    const pacCell = entityCell(state.pacman);
    const scheduleMode = MODE_SCHEDULE[state.modeIndex].mode;

    if (scheduleMode === "scatter") {
      return GHOST_TARGETS[ghost.name];
    }

    if (ghost.name === "blinky") {
      return pacCell;
    }

    if (ghost.name === "pinky") {
      return cellsAhead(pacCell, state.pacman.dir || state.pacman.desiredDir, 6);
    }

    if (ghost.name === "inky") {
      const ahead = cellsAhead(pacCell, state.pacman.dir || state.pacman.desiredDir, 2);
      const blinky = state.ghosts.find((item) => item.name === "blinky");
      const blinkyCell = entityCell(blinky);
      return nearestPassable({
        row: ahead.row + 2 * (blinkyCell.row - ahead.row),
        col: ahead.col + 2 * (blinkyCell.col - ahead.col),
      });
    }

    if (ghost.name === "clyde") {
      if (distanceCells(ghost, state.pacman) <= 8) {
        return GHOST_TARGETS.clyde;
      }
      return pacCell;
    }

    return pacCell;
  }

  function cellsAhead(start, direction, amount) {
    let target = { row: start.row, col: start.col };
    if (!direction) {
      return target;
    }

    for (let i = 0; i < amount; i += 1) {
      const next = neighborCell(target, direction);
      if (!next || !canEnterCell(next.row, next.col, true)) {
        break;
      }
      target = next;
    }

    return target;
  }

  function nearestPassable(target) {
    const start = {
      row: clamp(Math.round(target.row), 0, FIELD_HEIGHT - 1),
      col: clamp(Math.round(target.col), 0, FIELD_WIDTH - 1),
    };

    if (canEnterCell(start.row, start.col, false)) {
      return start;
    }

    const queue = [start];
    const seen = new Set([cellKey(start)]);

    for (let index = 0; index < queue.length; index += 1) {
      const cell = queue[index];
      for (const direction of DIR_LIST) {
        const next = {
          row: cell.row + direction.dr,
          col: cell.col + direction.dc,
        };
        const key = cellKey(next);
        if (
          seen.has(key) ||
          next.row < 0 ||
          next.row >= FIELD_HEIGHT ||
          next.col < 0 ||
          next.col >= FIELD_WIDTH
        ) {
          continue;
        }
        if (canEnterCell(next.row, next.col, false)) {
          return next;
        }
        seen.add(key);
        queue.push(next);
      }
    }

    return { row: 14, col: 15 };
  }

  function pathDistance(start, target) {
    if (!target) {
      return Infinity;
    }

    const safeTarget = nearestPassable(target);
    const queue = [{ row: start.row, col: start.col, dist: 0 }];
    const seen = new Set([cellKey(start)]);

    for (let index = 0; index < queue.length; index += 1) {
      const current = queue[index];
      if (current.row === safeTarget.row && current.col === safeTarget.col) {
        return current.dist;
      }

      for (const direction of DIR_LIST) {
        const next = neighborCell(current, direction);
        if (!next || !canEnterCell(next.row, next.col, false)) {
          continue;
        }
        const key = cellKey(next);
        if (seen.has(key)) {
          continue;
        }
        seen.add(key);
        queue.push({ row: next.row, col: next.col, dist: current.dist + 1 });
      }
    }

    return Infinity;
  }

  function moveEntity(entity, dt, pacmanRules) {
    if (!entity.dir) {
      return;
    }

    if (isCentered(entity)) {
      snapToCell(entity);
      if (!canMoveFromEntity(entity, entity.dir, pacmanRules)) {
        entity.dir = null;
        return;
      }

      const portal = portalDestination(entityCell(entity), entity.dir);
      if (portal) {
        entity.row = portal.row;
        entity.col = portal.col;
        return;
      }
    }

    const target = nextMovementTarget(entity);
    if (!target) {
      return;
    }

    const step = entity.speed * dt;
    if (entity.dir.dc !== 0) {
      const distance = Math.abs(target.col - entity.col);
      const move = Math.min(step, distance);
      entity.col += entity.dir.dc * move;
      entity.row = target.row;
      if (move >= distance) {
        entity.col = target.col;
      }
    } else {
      const distance = Math.abs(target.row - entity.row);
      const move = Math.min(step, distance);
      entity.row += entity.dir.dr * move;
      entity.col = target.col;
      if (move >= distance) {
        entity.row = target.row;
      }
    }
  }

  function nextMovementTarget(entity) {
    if (isCentered(entity)) {
      const cell = entityCell(entity);
      return neighborCell(cell, entity.dir);
    }

    if (entity.dir.dc > 0) {
      return { row: Math.round(entity.row), col: Math.ceil(entity.col - CENTER_EPSILON) };
    }
    if (entity.dir.dc < 0) {
      return { row: Math.round(entity.row), col: Math.floor(entity.col + CENTER_EPSILON) };
    }
    if (entity.dir.dr > 0) {
      return { row: Math.ceil(entity.row - CENTER_EPSILON), col: Math.round(entity.col) };
    }
    return { row: Math.floor(entity.row + CENTER_EPSILON), col: Math.round(entity.col) };
  }

  function legalDirections(cell, pacmanRules) {
    return DIR_LIST.filter((direction) => {
      const next = neighborCell(cell, direction);
      return next && canEnterCell(next.row, next.col, pacmanRules);
    });
  }

  function canMoveFromEntity(entity, direction, pacmanRules) {
    if (!direction) {
      return false;
    }
    const next = neighborCell(entityCell(entity), direction);
    return next && canEnterCell(next.row, next.col, pacmanRules);
  }

  function canEnterCell(row, col, pacmanRules) {
    const type = getCellType(row, col);
    if (type === null || type === TYPE.WALL) {
      return false;
    }
    return !(pacmanRules && type === TYPE.GHOST_HOUSE);
  }

  function getCellType(row, col) {
    if (row < 0 || row >= FIELD_HEIGHT || col < 0 || col >= FIELD_WIDTH) {
      return null;
    }
    return state.grid[row][col];
  }

  function neighborCell(cell, direction) {
    const portal = portalDestination(cell, direction);
    if (portal) {
      return portal;
    }

    const row = cell.row + direction.dr;
    const col = cell.col + direction.dc;
    if (row < 0 || row >= FIELD_HEIGHT || col < 0 || col >= FIELD_WIDTH) {
      return null;
    }

    return { row, col };
  }

  function portalDestination(cell, direction) {
    if (cell.row === 14 && cell.col === 0 && direction.name === "left") {
      return { row: 14, col: FIELD_WIDTH - 1 };
    }
    if (cell.row === 14 && cell.col === FIELD_WIDTH - 1 && direction.name === "right") {
      return { row: 14, col: 0 };
    }
    return null;
  }

  function reverseDirection(direction) {
    if (!direction) {
      return null;
    }
    return DIRECTIONS[OPPOSITE[direction.name]];
  }

  function isOppositeDirection(a, b) {
    return Boolean(a && b && OPPOSITE[a.name] === b.name);
  }

  function entityCell(entity) {
    return {
      row: clamp(Math.round(entity.row), 0, FIELD_HEIGHT - 1),
      col: clamp(Math.round(entity.col), 0, FIELD_WIDTH - 1),
    };
  }

  function isCentered(entity) {
    return (
      Math.abs(entity.row - Math.round(entity.row)) <= CENTER_EPSILON &&
      Math.abs(entity.col - Math.round(entity.col)) <= CENTER_EPSILON
    );
  }

  function snapToCell(entity) {
    entity.row = Math.round(entity.row);
    entity.col = Math.round(entity.col);
  }

  function distanceCells(a, b) {
    return Math.hypot(a.row - b.row, a.col - b.col);
  }

  function cellKey(cell) {
    return `${cell.row}:${cell.col}`;
  }

  function clamp(value, min, max) {
    return Math.max(min, Math.min(max, value));
  }

  function draw(now) {
    ctx.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    ctx.fillStyle = "#000";
    ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

    if (assets.maze) {
      ctx.drawImage(assets.maze, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    drawPellets(now);
    drawActors(now);
    drawHud();
    drawOverlay(now);
    updateStatusText();
  }

  function drawPellets(now) {
    const pulse = 0.5 + 0.5 * Math.sin(now / 110);

    for (let row = 0; row < FIELD_HEIGHT; row += 1) {
      for (let col = 0; col < FIELD_WIDTH; col += 1) {
        const type = state.grid[row][col];
        const point = cellCenter(row, col);

        if (type === TYPE.DOT) {
          ctx.fillStyle = "#f6f1d1";
          ctx.fillRect(point.x - 2, point.y - 2, 4, 4);
        }

        if (type === TYPE.SUPER) {
          const radius = 5 + pulse * 3;
          ctx.fillStyle = "#ffb43d";
          ctx.beginPath();
          ctx.arc(point.x, point.y, radius, 0, Math.PI * 2);
          ctx.fill();
        }
      }
    }
  }

  function drawActors(now) {
    if (!state.pacman) {
      return;
    }

    drawPacman(now);
    for (const ghost of state.ghosts) {
      drawGhost(ghost, now);
    }
  }

  function drawPacman(now) {
    const pacman = state.pacman;
    const point = entityCenter(pacman);
    let sprite;

    if (state.status === "dying") {
      const frameIndex = clamp(Math.floor(pacman.deathElapsed * 13), 0, PACMAN_DEATH_FRAMES.length - 1);
      sprite = PACMAN_DEATH_FRAMES[frameIndex];
    } else {
      const facing = (pacman.dir || pacman.desiredDir || DIRECTIONS.left).name;
      if (pacman.isChewing) {
        const frames = PACMAN_CHEW_FRAMES[facing];
        sprite = frames[pacman.mouthFrameIndex % frames.length];
      } else {
        sprite = PACMAN_STATIC_FRAMES[facing];
      }
    }

    drawSprite(sprite[0], sprite[1], point.x, point.y);
  }

  function drawGhost(ghost, now) {
    const point = entityCenter(ghost);
    let sprite;

    if (ghost.mode === "eaten") {
      const facing = (ghost.dir || DIRECTIONS.left).name;
      const eyes = {
        right: [8, 5],
        left: [9, 5],
        up: [10, 5],
        down: [11, 5],
      };
      sprite = eyes[facing];
    } else if (ghost.mode === "frightened") {
      const frame = Math.floor(now / 160) % 2;
      if (ghost.blink && Math.floor(now / 120) % 2 === 0) {
        sprite = frame === 0 ? [10, 4] : [11, 4];
      } else {
        sprite = frame === 0 ? [8, 4] : [9, 4];
      }
    } else {
      const spriteRow = GHOST_ROWS[ghost.name];
      const frame = Math.floor(now / 140) % 2;
      const facing = (ghost.dir || DIRECTIONS.left).name;
      const offsets = {
        right: [0, 1],
        left: [2, 3],
        up: [4, 5],
        down: [6, 7],
      };
      sprite = [offsets[facing][frame], spriteRow];
    }

    drawSprite(sprite[0], sprite[1], point.x, point.y);
  }

  function drawSprite(gridX, gridY, centerX, centerY) {
    if (!spriteSheet) {
      return;
    }
    ctx.drawImage(
      spriteSheet,
      gridX * SPRITE_SIZE,
      gridY * SPRITE_SIZE,
      SPRITE_SIZE,
      SPRITE_SIZE,
      Math.round(centerX - SPRITE_SIZE / 2),
      Math.round(centerY - SPRITE_SIZE / 2),
      SPRITE_SIZE,
      SPRITE_SIZE
    );
  }

  function drawHud() {
    ctx.fillStyle = "#000";
    ctx.fillRect(0, MAZE_HEIGHT, CANVAS_WIDTH, CANVAS_HEIGHT - MAZE_HEIGHT);
    ctx.fillStyle = "#f4f4f0";
    ctx.font = "700 16px Arial, Helvetica, sans-serif";
    ctx.textBaseline = "top";
    ctx.fillText("SCORE", 230, 565);
    ctx.fillText(String(state.score), 230, 582);
    ctx.fillText(`LEVEL ${state.level}`, 350, 574);

    for (let i = 0; i < state.lives; i += 1) {
      drawSprite(1, 0, 18 + i * 30, 581);
    }
  }

  function drawOverlay() {
    if (state.status === "ready" && assets.ready) {
      ctx.drawImage(assets.ready, 190, 295);
    }

    if (state.status === "level-clear") {
      drawCenterText("LEVEL CLEAR", "#ffd436");
    }

    if (state.status === "gameover") {
      if (assets.gameOver) {
        ctx.drawImage(assets.gameOver, 180, 300);
      } else {
        drawCenterText("GAME OVER", "#f35353");
      }
    }

    if (!state.started && state.status !== "gameover") {
      drawCenterText("PRESS START", "#ffd436", 250);
    }

    if (state.paused) {
      drawCenterText("PAUSED", "#f4f4f0");
    }
  }

  function drawCenterText(text, color, y = 300) {
    ctx.save();
    ctx.fillStyle = "rgba(0, 0, 0, 0.72)";
    ctx.fillRect(118, y - 30, 314, 58);
    ctx.fillStyle = color;
    ctx.font = "700 24px Arial, Helvetica, sans-serif";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText(text, CANVAS_WIDTH / 2, y);
    ctx.restore();
  }

  function cellCenter(row, col) {
    return {
      x: 8 + (col - VIRT_BORDERS / 2) * W_COEFF,
      y: 8 + row * H_COEFF,
    };
  }

  function entityCenter(entity) {
    return cellCenter(entity.row, entity.col);
  }

  function updateStatusText() {
    let nextStatus = "";

    if (state.status === "loading") {
      nextStatus = "Loading";
    } else if (state.paused) {
      nextStatus = `Paused. Score ${state.score}.`;
    } else if (state.status === "gameover") {
      nextStatus = `Game over. Final score ${state.score}.`;
    } else if (state.status === "level-clear") {
      nextStatus = `Level ${state.level} clear. Score ${state.score}.`;
    } else if (state.status === "ready") {
      nextStatus = `Ready. Level ${state.level}.`;
    } else {
      nextStatus = `Score ${state.score}. Level ${state.level}. Lives ${state.lives}. Pellets ${state.remainingPellets}.`;
    }

    setStatus(nextStatus);
  }

  function setStatus(nextStatus) {
    if (nextStatus === statusText) {
      return;
    }
    statusText = nextStatus;
    statusEl.textContent = nextStatus;
  }

  function loop(now) {
    const dt = Math.min((now - lastTime) / 1000, 0.05);
    lastTime = now;

    update(dt);
    draw(now);
    requestAnimationFrame(loop);
  }

  function bindInput() {
    let pointerStart = null;

    window.addEventListener("keydown", (event) => {
      const keyMap = {
        ArrowUp: "up",
        w: "up",
        W: "up",
        ArrowDown: "down",
        s: "down",
        S: "down",
        ArrowLeft: "left",
        a: "left",
        A: "left",
        ArrowRight: "right",
        d: "right",
        D: "right",
      };

      if (keyMap[event.key]) {
        event.preventDefault();
        setRequestedDirection(keyMap[event.key]);
      }

      if (event.key === " " || event.key === "p" || event.key === "P") {
        event.preventDefault();
        togglePause();
      }

      if (event.key === "r" || event.key === "R") {
        event.preventDefault();
        resetGame();
        startGame();
      }

      if (event.key === "m" || event.key === "M") {
        event.preventDefault();
        toggleMute();
      }
    });

    startButton.addEventListener("click", startGame);
    pauseButton.addEventListener("click", togglePause);
    muteButton.addEventListener("click", toggleMute);
    restartButton.addEventListener("click", () => {
      resetGame();
      startGame();
    });

    canvas.addEventListener("pointerdown", (event) => {
      event.preventDefault();
      pointerStart = {
        x: event.clientX,
        y: event.clientY,
      };
      canvas.setPointerCapture(event.pointerId);
    });

    canvas.addEventListener("pointerup", (event) => {
      if (!pointerStart) {
        return;
      }

      event.preventDefault();
      const dx = event.clientX - pointerStart.x;
      const dy = event.clientY - pointerStart.y;
      pointerStart = null;

      if (Math.hypot(dx, dy) < 18) {
        startGame();
        return;
      }

      if (Math.abs(dx) > Math.abs(dy)) {
        setRequestedDirection(dx > 0 ? "right" : "left");
      } else {
        setRequestedDirection(dy > 0 ? "down" : "up");
      }
    });

    canvas.addEventListener("pointercancel", () => {
      pointerStart = null;
    });

    for (const button of document.querySelectorAll("[data-dir]")) {
      button.addEventListener("pointerdown", (event) => {
        event.preventDefault();
        setRequestedDirection(button.dataset.dir);
      });
    }
  }

  configureAudio();
  bindInput();
  loadAssets()
    .then(() => {
      resetGame();
      lastTime = performance.now();
      requestAnimationFrame(loop);
    })
    .catch((error) => {
      setStatus(error.message);
      throw error;
    });
})();
