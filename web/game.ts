// ===== Nordic Warrior XL - Web Port =====
// Migrated from Java (AWT/Swing) to HTML5 Canvas + TypeScript

const WIDTH = 900;
const HEIGHT = 506;
const TILE_SIZE = 36;
const GRAVITY = 0.4;
const ASSET_BASE = 'res/graphics/';

// ===== Sound Effects =====

const audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)();

function playSfx(type: 'throw' | 'explode' | 'die'): void {
    if (audioCtx.state === 'suspended') audioCtx.resume();

    if (type === 'throw') {
        // Heavy thunk + low whoosh
        const t = audioCtx.currentTime;
        // Low thud impact
        const thud = audioCtx.createOscillator();
        const thudGain = audioCtx.createGain();
        thud.type = 'sine';
        thud.frequency.setValueAtTime(120, t);
        thud.frequency.exponentialRampToValueAtTime(50, t + 0.15);
        thudGain.gain.setValueAtTime(0.3, t);
        thudGain.gain.exponentialRampToValueAtTime(0.001, t + 0.15);
        thud.connect(thudGain).connect(audioCtx.destination);
        thud.start(t);
        thud.stop(t + 0.15);
        // Heavy whoosh
        const whooshLen = audioCtx.sampleRate * 0.2;
        const whooshBuf = audioCtx.createBuffer(1, whooshLen, audioCtx.sampleRate);
        const wd = whooshBuf.getChannelData(0);
        for (let i = 0; i < whooshLen; i++) {
            const env = Math.sin(Math.PI * i / whooshLen);
            wd[i] = (Math.random() * 2 - 1) * env;
        }
        const whoosh = audioCtx.createBufferSource();
        whoosh.buffer = whooshBuf;
        const wGain = audioCtx.createGain();
        wGain.gain.setValueAtTime(0.12, t);
        const wFilter = audioCtx.createBiquadFilter();
        wFilter.type = 'bandpass';
        wFilter.frequency.setValueAtTime(300, t);
        wFilter.Q.setValueAtTime(1.5, t);
        whoosh.connect(wFilter).connect(wGain).connect(audioCtx.destination);
        whoosh.start(t);
        whoosh.stop(t + 0.2);
    } else if (type === 'explode') {
        // Grand layered explosion
        const t = audioCtx.currentTime;
        // Layer 1: deep bass boom
        const boom = audioCtx.createOscillator();
        const boomGain = audioCtx.createGain();
        boom.type = 'sine';
        boom.frequency.setValueAtTime(80, t);
        boom.frequency.exponentialRampToValueAtTime(20, t + 0.6);
        boomGain.gain.setValueAtTime(0.4, t);
        boomGain.gain.exponentialRampToValueAtTime(0.001, t + 0.6);
        boom.connect(boomGain).connect(audioCtx.destination);
        boom.start(t);
        boom.stop(t + 0.6);
        // Layer 2: mid crunch
        const crunch = audioCtx.createOscillator();
        const crunchGain = audioCtx.createGain();
        crunch.type = 'sawtooth';
        crunch.frequency.setValueAtTime(200, t);
        crunch.frequency.exponentialRampToValueAtTime(40, t + 0.4);
        crunchGain.gain.setValueAtTime(0.15, t);
        crunchGain.gain.exponentialRampToValueAtTime(0.001, t + 0.4);
        crunch.connect(crunchGain).connect(audioCtx.destination);
        crunch.start(t);
        crunch.stop(t + 0.4);
        // Layer 3: long noise tail
        const noiseLen = audioCtx.sampleRate * 0.8;
        const noiseBuf = audioCtx.createBuffer(1, noiseLen, audioCtx.sampleRate);
        const nd = noiseBuf.getChannelData(0);
        for (let i = 0; i < noiseLen; i++) {
            nd[i] = (Math.random() * 2 - 1) * Math.pow(1 - i / noiseLen, 2);
        }
        const noise = audioCtx.createBufferSource();
        noise.buffer = noiseBuf;
        const nGain = audioCtx.createGain();
        nGain.gain.setValueAtTime(0.3, t);
        nGain.gain.exponentialRampToValueAtTime(0.001, t + 0.8);
        const nFilter = audioCtx.createBiquadFilter();
        nFilter.type = 'lowpass';
        nFilter.frequency.setValueAtTime(2000, t);
        nFilter.frequency.exponentialRampToValueAtTime(60, t + 0.8);
        noise.connect(nFilter).connect(nGain).connect(audioCtx.destination);
        noise.start(t);
        noise.stop(t + 0.8);
    } else if (type === 'die') {
        // Descending tone
        const osc = audioCtx.createOscillator();
        const gain = audioCtx.createGain();
        osc.type = 'square';
        osc.frequency.setValueAtTime(400, audioCtx.currentTime);
        osc.frequency.exponentialRampToValueAtTime(80, audioCtx.currentTime + 0.5);
        gain.gain.setValueAtTime(0.15, audioCtx.currentTime);
        gain.gain.linearRampToValueAtTime(0, audioCtx.currentTime + 0.5);
        osc.connect(gain).connect(audioCtx.destination);
        osc.start();
        osc.stop(audioCtx.currentTime + 0.5);
    }
}

// ===== Asset Loading =====

interface SheetMap {
    [key: string]: HTMLCanvasElement;
}

const sheets: SheetMap = {};

function processImage(img: HTMLImageElement): HTMLCanvasElement {
    const c = document.createElement('canvas');
    c.width = img.width;
    c.height = img.height;
    const ctx = c.getContext('2d')!;
    ctx.drawImage(img, 0, 0);
    const imageData = ctx.getImageData(0, 0, c.width, c.height);
    const d = imageData.data;
    for (let i = 0; i < d.length; i += 4) {
        if (d[i] >= 200 && d[i + 1] <= 50 && d[i + 2] >= 200) {
            d[i + 3] = 0;
        }
    }
    ctx.putImageData(imageData, 0, 0);
    return c;
}

function loadImage(src: string): Promise<HTMLImageElement> {
    return new Promise((resolve, reject) => {
        const img = new Image();
        img.onload = () => resolve(img);
        img.onerror = () => reject(new Error('Failed to load: ' + src));
        img.src = src;
    });
}

async function loadAssets(): Promise<void> {
    const assets: [string, string][] = [
        ['tiles', ASSET_BASE + 'spritesheet36backup.png'],
        ['player', ASSET_BASE + 'characters/player/playersheet.png'],
        ['skeleton', ASSET_BASE + 'characters/skeleton/skeletonsheet.png'],
        ['goblin', ASSET_BASE + 'characters/goblin/goblinsheet.png'],
        ['boss', ASSET_BASE + 'characters/Boss/boss_move.png'],
    ];
    const promises = assets.map(async ([name, src]) => {
        const img = await loadImage(src);
        sheets[name] = processImage(img);
    });
    await Promise.all(promises);
}

// ===== Tile Definitions =====

interface TileDef {
    srcX: number;
    srcY: number;
    w: number;
    h: number;
    solid: boolean;
}

const TILE_DEFS: { [id: number]: TileDef } = {
    1:  { srcX: 108, srcY: 0,   w: 36,  h: 36,  solid: true  }, // grass
    2:  { srcX: 0,   srcY: 0,   w: 36,  h: 36,  solid: false }, // grass2
    3:  { srcX: 72,  srcY: 0,   w: 36,  h: 36,  solid: false }, // grass3
    4:  { srcX: 36,  srcY: 108, w: 36,  h: 36,  solid: true  }, // stone1
    5:  { srcX: 36,  srcY: 144, w: 36,  h: 36,  solid: true  }, // stone2
    6:  { srcX: 36,  srcY: 0,   w: 36,  h: 36,  solid: true  }, // ground1
    7:  { srcX: 144, srcY: 0,   w: 180, h: 252, solid: false }, // tree
    8:  { srcX: 36,  srcY: 180, w: 108, h: 72,  solid: false }, // face2
    9:  { srcX: 0,   srcY: 288, w: 72,  h: 108, solid: false }, // budda
    10: { srcX: 0,   srcY: 36,  w: 72,  h: 72,  solid: false }, // dead1
    11: { srcX: 468, srcY: 0,   w: 144, h: 144, solid: false }, // dead2
    12: { srcX: 324, srcY: 0,   w: 144, h: 144, solid: false }, // gate
    13: { srcX: 72,  srcY: 252, w: 36,  h: 36,  solid: true  }, // edgeH
    14: { srcX: 108, srcY: 288, w: 36,  h: 36,  solid: true  }, // edgeV
    15: { srcX: 72,  srcY: 288, w: 36,  h: 36,  solid: false }, // eGrassH
    16: { srcX: 180, srcY: 252, w: 36,  h: 36,  solid: false }, // eGrassV
    17: { srcX: 108, srcY: 252, w: 36,  h: 36,  solid: true  }, // eGroundH
    18: { srcX: 144, srcY: 252, w: 36,  h: 36,  solid: true  }, // eGroundV
};

function isTileSolid(id: number): boolean {
    const def = TILE_DEFS[id];
    return def ? def.solid : false;
}

// ===== Sprite Frame =====

interface SpriteFrame {
    sheet: string;
    srcX: number;
    srcY: number;
    width: number;
    height: number;
}

function makeFrames(sheet: string, w: number, h: number, count: number, row: number): SpriteFrame[] {
    const frames: SpriteFrame[] = [];
    for (let i = 0; i < count; i++) {
        frames.push({
            sheet,
            srcX: i * w,
            srcY: row * TILE_SIZE,
            width: w,
            height: h,
        });
    }
    return frames;
}

// ===== SpriteAnimation =====

class SpriteAnimation {
    frames: SpriteFrame[];
    running = false;
    index = 0;
    time = 100;
    changeTime = 0;
    repetitive = true;
    requireFinish = false;
    locked = false;

    constructor(frames: SpriteFrame[], time = 100) {
        this.frames = frames;
        this.time = time;
    }

    start(time?: number): void {
        this.running = true;
        this.index = 0;
        this.changeTime = performance.now();
        if (time !== undefined) this.time = time;
        if (this.requireFinish) this.locked = true;
    }

    getSprite(): SpriteFrame {
        if (this.running) {
            const now = performance.now();
            if (now - this.changeTime >= this.time) {
                this.nextSprite();
                this.changeTime = now;
            }
        }
        return this.frames[this.index];
    }

    private nextSprite(): void {
        if (this.index < this.frames.length - 1) {
            this.index++;
        } else {
            this.locked = false;
            if (this.repetitive) {
                this.index = 0;
            } else {
                this.running = false;
            }
        }
    }
}

// ===== Input =====

class Input {
    keys: { [code: string]: boolean } = {};
    jump = false;
    left = false;
    right = false;
    down = false;
    attack = false;
    restart = false;
    esc = false;
    checkpoint = false;
    private attackLock = false;

    constructor() {
        window.addEventListener('keydown', (e) => this.onKeyDown(e));
        window.addEventListener('keyup', (e) => this.onKeyUp(e));
    }

    private onKeyDown(e: KeyboardEvent): void {
        this.keys[e.code] = true;
        if (e.code === 'Escape') this.esc = true;
        if (e.code === 'KeyX' && !this.attackLock) {
            this.attack = true;
            this.attackLock = true;
        }
        if (['Space', 'ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(e.code)) {
            e.preventDefault();
        }
    }

    private onKeyUp(e: KeyboardEvent): void {
        this.keys[e.code] = false;
        if (e.code === 'KeyX') {
            this.attackLock = false;
        }
    }

    update(): void {
        this.jump = this.keys['Space'] === true;
        this.right = this.keys['KeyD'] === true || this.keys['ArrowRight'] === true;
        this.left = this.keys['KeyA'] === true || this.keys['ArrowLeft'] === true;
        this.down = this.keys['KeyS'] === true || this.keys['ArrowDown'] === true;
        this.restart = this.keys['KeyR'] === true;
        this.checkpoint = this.keys['KeyC'] === true;
    }
}

// ===== Drawing Helpers =====

function drawSprite(
    ctx: CanvasRenderingContext2D,
    frame: SpriteFrame,
    destX: number,
    destY: number,
    dir: number = 1,
    redTint: number = 0,
): void {
    const sheet = sheets[frame.sheet];
    if (!sheet) return;

    if (dir === -1) {
        ctx.save();
        ctx.translate(destX + 35, 0);
        ctx.scale(-1, 1);
        ctx.drawImage(
            sheet,
            frame.srcX, frame.srcY, frame.width, frame.height,
            0, destY, frame.width, frame.height,
        );
        ctx.restore();
    } else {
        ctx.drawImage(
            sheet,
            frame.srcX, frame.srcY, frame.width, frame.height,
            destX, destY, frame.width, frame.height,
        );
    }

    if (redTint > 0) {
        ctx.save();
        ctx.globalAlpha = 0.3 + 0.2 * Math.sin(performance.now() / 60);
        ctx.globalCompositeOperation = 'source-atop';
        // Approximate with flashing opacity instead
        ctx.restore();
    }
}

// ===== Projectile =====

class AxeProjectile {
    x: number;
    y: number;
    dir: number;
    xOrigin: number;
    ny: number;
    speed = 10;
    range = 700;
    damage = 50;
    knockback = 20;
    freezetime = 100;
    removed = false;
    animation: SpriteAnimation;
    level: Level;

    constructor(x: number, y: number, dir: number, level: Level) {
        this.x = x;
        this.y = y;
        this.dir = dir;
        this.xOrigin = x;
        this.ny = level.player.yv;
        this.level = level;
        this.animation = new SpriteAnimation(makeFrames('player', 36, 72, 8, 10), 30);
        this.animation.start();
    }

    update(): void {
        // Check tile collision
        const checkX = Math.floor((this.x + 18) / TILE_SIZE);
        const checkY = this.level.height - Math.floor((this.y + 32) / TILE_SIZE) - 1;
        if (checkX >= 0 && checkY >= 0 && checkX < this.level.width && checkY < this.level.height) {
            if (isTileSolid(this.level.tiles[checkX + checkY * this.level.width])) {
                this.removed = true;
                return;
            }
        }

        if (Math.abs(this.xOrigin - this.x) > this.range) {
            this.removed = true;
        } else {
            this.x += this.dir * this.speed;
            this.y += this.ny;
        }
    }

    getSprite(): SpriteFrame {
        return this.animation.getSprite();
    }
}

// ===== Mob Base =====

class Mob {
    x = 0;
    y = 0;
    xv = 0;
    yv = 0;
    dir = 1;
    health = 0;
    hitboxWidth = 0;
    hitboxHeight = 0;
    onGround = false;
    dead = false;
    moving = false;
    removed = false;
    godmode = false;
    damage = 0;
    graceTime = 0;
    freezeTimer = 0;
    animation!: SpriteAnimation;
    animations: SpriteAnimation[] = [];
    projectiles: AxeProjectile[] = [];
    level!: Level;

    // Origin position for reset
    originX = 0;
    originY = 0;

    update(): void {
        // Update projectiles and check hits
        for (const p of this.projectiles) {
            p.update();
            const px = p.x + p.getSprite().width / 2;
            const py = p.y + 32;
            for (const m of this.level.mobs) {
                if (px >= m.x && px <= m.x + m.hitboxWidth &&
                    py >= m.y && py <= m.y + m.hitboxHeight && !m.dead) {
                    m.attackThis(this, p.damage, p.knockback, p.freezetime);
                    p.removed = true;
                }
            }
        }
        this.projectiles = this.projectiles.filter(p => !p.removed);

        if (this.health <= 0 || this.dead) {
            this.kill();
            return;
        }

        this.move(this.xv, this.yv);

        if (!this.onGround && !this.godmode) {
            this.yv -= this.level.gravity;
        }

        // Enemy-player collision (not for player itself)
        if (!(this instanceof Player)) {
            if (this.intersectsPlayer() && !this.level.player.dead) {
                this.level.player.attackThis(this, this.damage, 20, 100);
            }
        }
    }

    intersectsPlayer(): boolean {
        const p = this.level.player;
        return rectsIntersect(
            this.x, this.y, this.hitboxWidth, this.hitboxHeight,
            p.x, p.y, p.hitboxWidth, p.hitboxHeight,
        );
    }

    kill(): void {
        this.removed = true;
    }

    move(xv: number, yv: number): boolean {
        let collision = false;

        if (xv > 0) this.dir = 1;
        else if (xv < 0) this.dir = -1;

        if (this.freezeTimer <= this.level.time) {
            if (!this.tileCollision(this.x + xv, this.y) && xv !== 0) {
                this.x += xv;
                this.moving = true;
            } else {
                this.xv = 0;
                this.moving = false;
                collision = true;
            }
        }

        if (!this.tileCollision(this.x, this.y + yv)) {
            this.y += yv;
        } else {
            if (yv < 0) {
                this.onGround = true;
                // Snap to ground
                let yt = Math.floor(this.y);
                while (!this.tileCollision(this.x, yt - 1)) {
                    yt--;
                }
                this.y = yt;
                collision = true;
            }
            this.yv = 0;
        }

        if (this.onGround && this.y > 0) {
            if (!this.tileCollision(this.x, this.y - 1)) {
                this.onGround = false;
            }
        }

        return collision;
    }

    tileCollision(x: number, y: number): boolean {
        if (y < -this.hitboxHeight) {
            this.kill();
            return false;
        }
        if (x + this.hitboxWidth < 0) return false;
        if (x > this.level.width * TILE_SIZE) return false;
        if (this.godmode) return false;

        for (let yc = 0; yc < this.hitboxHeight; yc += 10) {
            for (let xc = 0; xc < this.hitboxWidth; xc += 10) {
                const gx = Math.floor((xc + x) / TILE_SIZE);
                const gy = this.level.height - Math.floor((y + yc) / TILE_SIZE) - 1;
                if (gx < 0 || gy < 0 || gx >= this.level.width || gy >= this.level.height) continue;
                if (isTileSolid(this.level.tiles[gx + gy * this.level.width])) {
                    return true;
                }
            }
        }
        return false;
    }

    push(units: number): void {
        if (!this.tileCollision(this.x + units, this.y)) {
            this.x += units;
        } else if (Math.abs(units) > 1) {
            this.push(units > 0 ? units - 1 : units + 1);
        }
    }

    attackThis(attacker: Mob, damage: number, knockback: number, freezems: number): void {
        if (this.godmode) return;
        this.freezeTimer = this.level.time + freezems;
        this.push(knockback * attacker.dir);
        this.health -= damage;
        this.graceTime = this.level.time + 2000;
    }

    isProtected(): boolean {
        return this.graceTime > this.level.time;
    }

    setSpriteAnimation(anim: SpriteAnimation, time: number): boolean {
        if (!this.animation.locked && this.animation !== anim) {
            this.animation = anim;
            this.animation.start(time);
            return true;
        }
        return false;
    }

    getSprite(): SpriteFrame {
        return this.animation.getSprite();
    }

    clone(): Mob {
        const m = new (this.constructor as new () => Mob)();
        m.x = this.originX;
        m.y = this.originY;
        m.originX = this.originX;
        m.originY = this.originY;
        m.level = this.level;
        m.initMob();
        return m;
    }

    initMob(): void {}
}

// ===== Player =====

class Player extends Mob {
    key!: Input;
    axeTimer = 0;
    xOrigin: number;
    yOrigin: number;
    hitboxW = 30;
    hitboxH = 65;

    constructor(x: number = 36, y: number = 36) {
        super();
        this.x = this.xOrigin = x;
        this.y = this.yOrigin = y;
        this.initPlayer();
    }

    initPlayer(): void {
        this.animations = [
            new SpriteAnimation(makeFrames('player', 36, 72, 5, 12), 150), // stance
            new SpriteAnimation(makeFrames('player', 36, 72, 4, 0), 150),  // walk
            new SpriteAnimation(makeFrames('player', 108, 72, 2, 8), 200), // axe throw
            new SpriteAnimation(makeFrames('player', 36, 72, 8, 14), 150), // death
        ];
        this.animations[2].locked = true;
        this.animations[2].requireFinish = true;
        this.animations[3].locked = true;
        this.animations[3].requireFinish = true;
        this.animations[0].start(150);
        this.animation = this.animations[0];
        this.health = 30;
        this.hitboxWidth = 30;
        this.hitboxHeight = 65;
    }

    update(): void {
        super.update();

        if (this.dead) {
            if (!this.animation.locked) {
                this.level.resetLevel();
            }
            this.move(0, this.yv);
            return;
        }

        if (!this.key) return;

        if (this.key.restart) {
            this.xOrigin = this.level.spawnX;
            this.yOrigin = this.level.spawnY;
            this.level.resetLevel();
            return;
        }

        if (this.key.left || this.key.right) {
            this.dir = this.key.left ? -1 : 1;
            this.xv = 4 * this.dir;
            this.moving = true;
            this.setSpriteAnimation(this.animations[1], 150);
        } else {
            this.xv = 0;
            this.setSpriteAnimation(this.animations[0], 150);
        }

        if (this.key.jump && (this.onGround || this.godmode)) {
            this.yv = 8;
            this.onGround = false;
        } else if (this.key.down && this.godmode) {
            this.yv = -8;
        } else if (this.godmode) {
            this.yv = 0;
        }

        if (this.key.attack && this.axeTimer < this.level.time) {
            this.key.attack = false;
            this.throwAxe();
            this.setSpriteAnimation(this.animations[2], 200);
        }
    }

    private throwAxe(): void {
        const cooldown = 600;
        if (this.level.time > this.axeTimer) {
            this.projectiles.push(new AxeProjectile(this.x, this.y, this.dir, this.level));
            this.axeTimer = this.level.time + cooldown;
            playSfx('throw');
        }
    }

    attackThis(attacker: Mob, damage: number, knockback: number, freezems: number): void {
        if (this.graceTime < this.level.time) {
            super.attackThis(attacker, damage, knockback, freezems);
            this.graceTime = this.level.time + 2000;
        }
    }

    kill(): void {
        if (this.godmode || this.dead) return;
        this.dead = true;
        this.animation = this.animations[3];
        this.animation.start(150);
        playSfx('die');
    }
}

// ===== Enemies =====

class Skeleton extends Mob {
    constructor() {
        super();
    }

    initMob(): void {
        this.animation = new SpriteAnimation(makeFrames('skeleton', 36, 72, 4, 0), 200);
        this.animation.start(200);
        this.health = 100;
        this.hitboxWidth = 35;
        this.hitboxHeight = 70;
        this.xv = 2;
        this.damage = 10;
    }

    update(): void {
        super.update();
        if (this.tileCollision(this.x + this.xv, this.y)) {
            this.xv *= -1;
        }
    }
}

class Goblin extends Mob {
    static exploding = 0;
    blastRadius = 100;
    destructTime = 2000;
    explodedTime = 0;

    constructor() {
        super();
    }

    initMob(): void {
        this.xv = 3;
        this.damage = 0;
        this.health = 1;
        this.hitboxWidth = 72;
        this.hitboxHeight = 35;
        this.animations = [
            new SpriteAnimation(makeFrames('goblin', 72, 72, 4, 0), 200),
            new SpriteAnimation(makeFrames('goblin', 72, 72, 1, 2), 400),
        ];
        this.animations[1].locked = true;
        this.animations[1].requireFinish = true;
        this.animation = this.animations[0];
        this.animation.start(200);
    }

    update(): void {
        if (this.removed) return;

        if (this.dead) {
            if (this.level.time - this.explodedTime > this.destructTime) {
                Goblin.exploding--;
                this.removed = true;
                this.level.shake = 0;
            } else {
                this.level.shake = Math.floor(
                    10 / (((this.level.time - this.explodedTime) / 100) + 1) * (Goblin.exploding + 1),
                );
            }
            return;
        }

        if (this.tileCollision(this.x + this.xv, this.y)) {
            this.xv *= -1;
        }

        this.move(this.xv, this.yv);

        if (!this.onGround && !this.godmode) {
            this.yv -= this.level.gravity;
        }

        if (this.intersectsPlayer() && !this.level.player.dead) {
            this.kill();
        }
    }

    attackThis(_attacker: Mob, _damage: number, _knockback: number, _freezems: number): void {
        if (!this.dead) this.kill();
    }

    kill(): void {
        if (this.dead) return;
        this.dead = true;
        this.selfDestruct();
        this.explodedTime = this.level.time;
    }

    private selfDestruct(): void {
        Goblin.exploding++;
        this.destructTime += 10 * Math.floor(Math.random() * 50);
        playSfx('explode');
        if (this.animations[1]) {
            this.animation = this.animations[1];
            this.animation.start(400);
        }

        const cx = this.x + this.hitboxWidth / 2;
        const cy = this.y + this.hitboxHeight / 2;

        for (const e of this.level.mobs) {
            const ex = e.x + this.hitboxWidth / 2;
            const ey = e.y + this.hitboxHeight / 2;
            if (ex > cx - this.blastRadius && ex < cx + this.blastRadius &&
                ey > cy - this.blastRadius && ey < cy + this.blastRadius &&
                e !== this && !e.dead) {
                e.kill();
            }
        }

        const p = this.level.player;
        if (!p.dead) {
            const px = p.x + this.hitboxWidth / 2;
            const py = p.y + this.hitboxHeight / 2;
            if (px > cx - this.blastRadius && px < cx + this.blastRadius &&
                py > cy - this.blastRadius && py < cy + this.blastRadius) {
                p.kill();
            }
        }

        this.xv = 0;
    }
}

class Boss extends Mob {
    constructor() {
        super();
    }

    initMob(): void {
        this.animation = new SpriteAnimation(makeFrames('boss', 144, 144, 4, 0), 100);
        this.animation.start(100);
        this.health = 200;
        this.hitboxWidth = 100;
        this.hitboxHeight = 100;
        this.damage = 20;
    }

    update(): void {
        super.update();
        this.yv = 0;
    }
}

// ===== Utility =====

function rectsIntersect(
    x1: number, y1: number, w1: number, h1: number,
    x2: number, y2: number, w2: number, h2: number,
): boolean {
    return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
}

// ===== Level =====

interface Rect {
    x: number;
    y: number;
    w: number;
    h: number;
}

interface MobSpawn {
    id: number;
    x: number;
    y: number;
}

class Level {
    width = 0;
    height = 0;
    tiles: number[] = [];
    mobs: Mob[] = [];
    originMobs: MobSpawn[] = [];
    player!: Player;
    gravity = GRAVITY;
    speed = 1.0;
    time = 0;
    startTime = 0;
    shake = 0;
    won = false;
    finnish: Rect = { x: -100, y: -100, w: 72, h: 72 };
    finishTime = 0;
    spawnX = 0;
    spawnY = 0;
    levelOffset = 0;

    loadFromData(data: string, input: Input): void {
        const lines = data.trim().split('\n');

        // Line 1: width,height
        const [w, h] = lines[0].split(',').map(Number);
        this.width = w;
        this.height = h;

        // Line 2: tile data
        this.tiles = lines[1].split(',').map(Number);

        // Line 3: mob data
        if (lines[2] && lines[2].length > 0) {
            const mobParts = lines[2].split(';');
            for (const part of mobParts) {
                const vals = part.split(',').map(Number);
                if (vals.length >= 3) {
                    this.originMobs.push({ id: vals[0], x: vals[1], y: vals[2] });
                }
            }
        }

        // Spawn mobs
        for (const ms of this.originMobs) {
            const mob = this.createMob(ms.id, ms.x, ms.y);
            if (mob) this.mobs.push(mob);
        }

        // Line 4: player spawn
        let px = 36, py = 36;
        if (lines[3]) {
            const pvals = lines[3].split(',').map(Number);
            if (pvals.length >= 2) {
                px = pvals[0];
                py = pvals[1];
            }
        }
        this.player = new Player(px, py);
        this.player.level = this;
        this.player.key = input;
        this.spawnX = px;
        this.spawnY = py;

        // Line 5: finish area
        if (lines[4]) {
            const fvals = lines[4].split(',').map(Number);
            if (fvals.length >= 4) {
                this.finnish = { x: fvals[0], y: fvals[1], w: fvals[2], h: fvals[3] };
            }
        }

        this.startTime = performance.now();
        this.levelOffset = -TILE_SIZE * this.height + HEIGHT;
    }

    createMob(id: number, x: number, y: number): Mob | null {
        let mob: Mob;
        if (id === 0) mob = new Skeleton();
        else if (id === 1) mob = new Goblin();
        else if (id === 2) mob = new Boss();
        else return null;
        mob.x = x;
        mob.y = y;
        mob.originX = x;
        mob.originY = y;
        mob.level = this;
        mob.initMob();
        return mob;
    }

    update(): void {
        this.time = performance.now() - this.startTime;

        this.player.update();

        for (let i = this.mobs.length - 1; i >= 0; i--) {
            this.mobs[i].update();
            if (this.mobs[i].removed) {
                this.mobs.splice(i, 1);
            }
        }

        // Check finish
        if (rectsIntersect(
            this.finnish.x, this.finnish.y, this.finnish.w, this.finnish.h,
            this.player.x, this.player.y, this.player.hitboxWidth, this.player.hitboxHeight,
        )) {
            this.won = true;
        }

        if (this.won && this.finishTime === 0) {
            this.finishTime = this.time + 3000;
        }
    }

    resetLevel(): void {
        this.mobs = [];
        for (const ms of this.originMobs) {
            const mob = this.createMob(ms.id, ms.x, ms.y);
            if (mob) this.mobs.push(mob);
        }
        this.player.health = 30;
        this.player.dead = false;
        this.player.xv = 0;
        this.player.yv = 0;
        this.player.x = this.player.xOrigin;
        this.player.y = this.player.yOrigin;
        this.player.initPlayer();
        this.won = false;
        this.finishTime = 0;
        this.shake = 0;
        Goblin.exploding = 0;
    }

    setCheckpoint(): void {
        this.player.xOrigin = this.player.x;
        this.player.yOrigin = this.player.y;
    }

    render(ctx: CanvasRenderingContext2D): void {
        // Clear with background color
        ctx.fillStyle = '#6b76b7';
        ctx.fillRect(0, 0, WIDTH, HEIGHT);

        // Camera
        const cameraX = this.player.x - WIDTH / 2;
        const startFollow = HEIGHT * 3 / 5;
        const cameraYShift = this.player.y > startFollow
            ? startFollow - this.player.y
            : 0;

        // Screen shake
        let shakeX = 0, shakeY = 0;
        if (this.shake > 0) {
            shakeX = Math.floor(Math.random() * this.shake) - this.shake / 2;
            shakeY = Math.floor(Math.random() * this.shake) - this.shake / 2;
        }

        ctx.save();
        ctx.translate(shakeX, shakeY);

        // Render tiles
        for (let gy = 0; gy < this.height; gy++) {
            for (let gx = 0; gx < this.width; gx++) {
                const tileId = this.tiles[gx + gy * this.width];
                if (tileId === 0 || tileId === undefined) continue;
                const def = TILE_DEFS[tileId];
                if (!def) continue;

                const screenX = gx * TILE_SIZE - cameraX;
                const screenY = gy * TILE_SIZE + this.levelOffset - cameraYShift;

                // Culling
                if (screenX + def.w < -50 || screenX > WIDTH + 50) continue;
                if (screenY + def.h < -50 || screenY > HEIGHT + 50) continue;

                drawSprite(ctx, {
                    sheet: 'tiles',
                    srcX: def.srcX,
                    srcY: def.srcY,
                    width: def.w,
                    height: def.h,
                }, screenX, screenY);
            }
        }

        // Render mobs
        for (const mob of this.mobs) {
            const screenX = mob.x - cameraX;
            const screenY = HEIGHT - mob.y - 70 - cameraYShift;
            drawSprite(ctx, mob.getSprite(), screenX, screenY, mob.dir);
        }

        // Render player
        const sprite = this.player.getSprite();
        const playerScreenX = WIDTH / 2 + this.player.dir * -1 * (sprite.width / 2 - 18);
        const playerScreenY = this.player.y > startFollow
            ? HEIGHT - startFollow - 70
            : HEIGHT - this.player.y - 70;

        // Player flashing when invulnerable
        if (this.player.isProtected()) {
            ctx.globalAlpha = 0.4 + 0.4 * Math.sin(performance.now() / 50);
        }
        drawSprite(ctx, sprite, playerScreenX, playerScreenY, this.player.dir);
        ctx.globalAlpha = 1;

        // Render projectiles
        for (const proj of this.player.projectiles) {
            const px = proj.x - cameraX;
            const py = HEIGHT - proj.y - 75 - cameraYShift;
            drawSprite(ctx, proj.getSprite(), px, py, proj.dir);
        }

        ctx.restore();

        // Level won text
        if (this.won) {
            ctx.fillStyle = '#fff';
            ctx.font = '28px sans-serif';
            ctx.textAlign = 'center';
            ctx.fillText('Level Completed!', WIDTH / 2, HEIGHT / 2);
            ctx.textAlign = 'left';
        }
    }
}

// ===== Map Data (embedded demo maps) =====

const MAP_DATA: { [name: string]: string } = {
    Demo: `100,16
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,7,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,4,0,0,0,9,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,10,0,0,0,0,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,8,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,3,3,3,3,3,3,5,5,5,3,3,3,3,3,3,3,15,0,0,0,0,0,0,0,0,0,16,3,0,0,0,15,0,0,0,0,0,0,0,0,11,0,0,0,0,0,0,0,0,0,12,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,12,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,13,3,3,0,0,0,0,0,3,3,14,1,1,1,1,13,15,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,9,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,0,0,11,0,0,0,0,4,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,17,1,1,1,1,1,1,1,1,1,18,6,6,6,6,17,13,15,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,0,0,0,0,0,0,0,4,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,17,13,3,3,3,3,3,3,0,0,0,0,3,0,0,0,0,3,0,0,0,0,3,0,0,10,0,0,0,0,0,0,0,10,0,0,0,0,0,0,16,3,3,3,3,3,3,3,3,0,0,0,0,3,3,3,3,5,5,15,0,0,0,0,0,0,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,0,0,0,0,4,4,4,4,4,4,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,14,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,13,3,0,0,0,0,3,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,0,0,0,0,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4
0,152,245;0,65,237;1,678,171;1,755,173;1,847,174;1,897,173;0,1217,231;0,2121,100;0,2187,100;1,2358,112;1,2403,110;1,2465,110;0,3376,115;0,3428,109;0,3509,120;1,3378,75;1,3429,75;1,3490,80;0,2247,106;
436,251
2988,82,72,72`,
    Franzjump: `200,16
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,4,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,10,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,7,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,0,0,0,0,0,0,0,4,0,0,0,4,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,4,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,4,4,4,4,4,4,4,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,4,4,0,0,0,0,4,4,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,4,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,4,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,4,4,0,0,0,0,4,4,0,0,0,0,4,4,0,0,0,0,0,0,0,0,0,12,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,4,0,0,0,0,0,0,4,0,0,4,0,0,0,0,0,0,0,0,0,0,4,4,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,0,0,4,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,4,0,0,0,0,0,4,0,0,0,0,0,0,9,0,0,0,0,0,0,0,9,0,0,0,0,0,10,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,4,0,0,4,0,0,0,0,0,0,4,0,0,4,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,4,0,0,0,0,4,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,4,0,0,0,0,4,0,0,0,0,0,0,0,0,0,4,4,4,0,0,4,4,4,4,4,4,4,4,4,4,4,0,0,0,0,0,0,0,0,4,0,0,4,0,0,0,0,0,4,4,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,16,3,3,3,3,3,3,3,3,3,3,15,0,0,0,0,0,4,0,0,4,0,0,4,0,0,0,0,0,4,4,0,0,4,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,0,0,0,0,4,0,0,0,0,0,4,0,0,0,0,0,16,3,3,3,0,0,0,3,3,3,3,15,0,0,14,1,1,1,1,1,1,1,1,1,1,13,0,0,4,0,0,4,0,0,4,0,0,4,0,0,0,0,0,4,4,0,0,4,0,0,0,0,0,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,4,4,0,0,0,0,0,14,1,1,1,1,1,1,1,1,1,1,13,0,0,18,6,6,6,6,6,6,6,6,6,6,17,0,0,4,0,0,4,0,0,4,0,0,4,0,0,0,0,0,4,4,0,0,4,0,0,0,0,0,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,4,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,18,6,6,6,6,6,6,6,6,6,6,17,0,0,6,6,6,6,6,6,6,6,6,6,6,6,0,0,4,0,0,4,0,0,4,0,0,4,0,0,0,0,0,4,4,0,0,4,0,0,0,0,0,4,4,4,4,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,4,4,4,4,4,4,0,0,0,4,0,0,0,4,0,0,0,4,0,0,0,4,0,0,0,0,0,0,0,4,4,4,4,4,4,4,4,4,4,4,4,4,0,0,0,0,0,6,6,6,6,6,6,6,6,6,6,6,6,0,0
1,2389,75;1,2437,72;1,2511,78;1,2553,79;1,2621,76;1,2657,72;1,2689,71;1,2696,67;1,2629,69;1,2571,77;1,2504,76;1,2460,80;1,2381,82;1,2356,83;1,2494,73;1,2595,78;1,2688,68;1,2709,70;1,2660,86;1,2573,80;1,2495,79;1,2439,75;1,2393,73;0,5352,204;0,5494,201;1,6111,74;1,6167,66;1,6171,66;1,6196,62;1,6270,63;1,6297,70;1,6312,70;0,892,564;0,833,563;0,785,570;0,753,567;0,713,566;0,681,567;0,637,569;0,605,565;0,560,563;0,514,568;0,480,570;0,440,558;1,436,558;1,423,557;1,423,557;1,427,554;1,434,552;1,455,556;1,455,563;1,443,555;1,446,550;1,482,557;1,479,559;1,443,538;1,450,519;1,454,537;1,435,548;1,4950,85;0,5407,209;0,5339,213;0,5466,217;0,5258,379;0,5261,379;0,5337,362;0,5320,367;1,5344,216;1,5300,379;1,6341,57;1,6355,77;
132,181
6876,128,72,72`,
    Franzmaze: `200,16
4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,4,4,4,4,4,4,4,0,0,0,4,4,4,0,0,0,4,4,4,4,4,4,4,4,4,4,4,0,0,0,4,4,4,4,4,4,4,4,4,4,4,0,0,0,4,4,4,4,4,4,4,4,4,0,0,0,0,0,4,0,0,0,0,4,0,0,0,0,4,4,0,0,0,0,4,0,0,4,0,0,0,4,0,0,0,0,4,0,0,0,4,0,0,0,4,0,0,0,4,0,0,4,0,0,4,0,4,0,0,0,4,0,0,0,4,4,4,0,0,0,4,0,4,4,0,0,0,0,4,4,0,0,0,0,4,0,0,0,0,4,0,0,0,0,4,4,0,0,0,4,0,0,0,4,0,0,0,0,4,0,0,0,0,0,4,0,0,0,0,4,0,0,4,0,0,0,4,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,4,4,4,4,4,4,4,4,0,0,0,0,4,0,0,0,4,4,4,4,4,4,4,4,4,4,4,0,0,0,0,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,0,0,0,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,0,0,0,4,4,4,4,4,4,4,4,4,4,4,4,4,0,0,0,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,0,0,0,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4
0,1805,436;0,1813,437;0,1843,431;0,1845,431;0,1861,433;0,1900,435;0,1945,433;0,1963,430;0,2019,433;0,2035,435;0,2031,254;0,2016,253;0,1990,262;0,2015,263;0,2067,256;0,2105,258;0,2143,257;0,2191,258;0,2208,260;0,2612,235;0,2657,239;0,2694,240;0,2750,240;0,2812,235;0,2856,234;0,3276,248;0,3309,253;0,3365,255;0,3428,252;0,3492,256;0,3537,255;0,4127,237;0,4155,238;0,4204,235;0,4263,237;0,4343,245;0,4361,244;0,4746,247;0,4777,253;0,4831,243;0,4831,243;0,4895,246;0,4930,250;0,4982,242;0,5078,237;0,5449,253;0,5450,253;0,5467,253;0,5539,246;0,5599,245;0,5603,247;0,5644,252;0,6178,253;0,6213,252;0,6264,254;0,6275,253;0,6563,265;0,6557,250;0,6584,243;0,6590,245;0,6653,77;0,6622,75;0,6612,75;0,6567,76;0,6528,81;0,6508,85;0,6474,85;0,6438,82;0,6396,78;0,6209,76;0,6187,73;0,6159,79;0,6133,81;0,5514,70;0,5474,76;0,5386,80;0,5336,76;0,5274,76;0,5232,81;0,4408,60;0,4378,78;0,4331,79;0,4290,76;0,4250,76;0,4223,78;0,3692,80;0,3680,82;0,3629,82;0,3611,83;0,3579,83;0,3518,90;0,3493,88;0,3504,-19;0,2696,58;0,2696,58;0,2683,59;0,2602,70;0,2557,72;0,2521,67;0,2488,63;0,2452,62;0,2317,90;0,2258,88;0,2210,92;0,2181,85;1,554,415;1,554,416;1,541,415;1,531,416;1,531,416;1,530,416;1,520,417;1,514,418;1,516,419;1,518,420;1,542,428;1,552,434;1,546,445;1,537,454;1,539,444;1,534,437;1,534,444;1,537,439;1,527,419;1,527,430;1,527,440;1,2391,270;1,2417,265;1,2470,264;1,2504,262;1,2534,257;1,2415,267;1,2337,263;1,3037,84;1,3091,87;1,3150,81;1,3231,79;1,3270,84;1,3216,85;1,3182,86;1,4546,263;1,4546,263;1,4584,263;1,4621,260;1,4676,253;1,4677,254;1,4658,74;1,4656,89;1,4714,89;1,4749,85;1,4722,80;1,4708,80;1,6471,249;1,6480,250;1,6463,247;1,6431,245;1,6405,242;1,6333,61;1,6314,67;1,6319,68;1,6350,63;
36,36
-100,-100,72,72`,
};

// ===== Game Class =====

class Game {
    canvas: HTMLCanvasElement;
    ctx: CanvasRenderingContext2D;
    input: Input;
    level: Level | null = null;
    running = false;
    lastTime = 0;
    delta = 0;
    fps = 0;
    frameCount = 0;
    fpsTime = 0;

    constructor() {
        this.canvas = document.getElementById('game') as HTMLCanvasElement;
        this.ctx = this.canvas.getContext('2d')!;
        this.ctx.imageSmoothingEnabled = false;
        this.input = new Input();
    }

    startLevel(name: string): void {
        const data = MAP_DATA[name];
        if (!data) return;

        this.level = new Level();
        this.level.loadFromData(data, this.input);

        document.getElementById('menu')!.style.display = 'none';
        document.getElementById('hud')!.style.display = 'block';

        if (!this.running) {
            this.running = true;
            this.lastTime = performance.now();
            this.fpsTime = this.lastTime;
            requestAnimationFrame((t) => this.loop(t));
        }
    }

    returnToMenu(): void {
        this.level = null;
        this.running = false;
        document.getElementById('menu')!.style.display = 'flex';
        document.getElementById('hud')!.style.display = 'none';
    }

    openEditor(): void {
        document.getElementById('menu')!.style.display = 'none';
        document.getElementById('editor-wrapper')!.classList.add('active');
        if (!mapEditor) {
            mapEditor = new MapEditor();
            (window as any).mapEditor = mapEditor;
        }
        mapEditor.open();
    }

    startLevelFromData(data: string): void {
        this.level = new Level();
        this.level.loadFromData(data, this.input);
        document.getElementById('menu')!.style.display = 'none';
        document.getElementById('hud')!.style.display = 'block';
        if (!this.running) {
            this.running = true;
            this.lastTime = performance.now();
            this.fpsTime = this.lastTime;
            requestAnimationFrame((t) => this.loop(t));
        }
    }

    loop(now: number): void {
        if (!this.running) return;

        const ns = 1000 / 60;
        this.delta += (now - this.lastTime) / ns;
        this.lastTime = now;

        let updated = false;
        while (this.delta >= 1) {
            this.delta--;
            this.update();
            updated = true;
        }

        if (updated) {
            this.render();
        }

        // FPS counter
        this.frameCount++;
        if (now - this.fpsTime >= 1000) {
            this.fps = this.frameCount;
            this.frameCount = 0;
            this.fpsTime = now;
        }

        requestAnimationFrame((t) => this.loop(t));
    }

    update(): void {
        if (!this.level) return;

        this.input.update();

        // ESC to return to menu
        if (this.input.esc) {
            this.input.esc = false;
            this.returnToMenu();
            return;
        }

        // Checkpoint (consume flag so it only fires once per press)
        if (this.input.checkpoint) {
            this.input.checkpoint = false;
            this.level.setCheckpoint();
        }

        this.level.update();

        // Auto return after level won
        if (this.level.won && this.level.finishTime > 0 && this.level.time > this.level.finishTime) {
            this.returnToMenu();
        }

        // Update HUD
        const healthFill = document.getElementById('health-fill')!;
        const pct = Math.max(0, (this.level.player.health / 30) * 100);
        healthFill.style.width = pct + '%';
        healthFill.style.background = pct > 50 ? '#4a4' : pct > 25 ? '#ca4' : '#c44';

        const hudText = document.getElementById('hud-text')!;
        hudText.textContent = `FPS: ${this.fps}`;
    }

    render(): void {
        if (!this.level) return;
        this.level.render(this.ctx);
    }
}

// ===== Map Editor =====

const TILE_NAMES = [
    'Grass', 'Grass 2', 'Grass 3', 'Stone 1', 'Stone 2', 'Dirt',
    'Tree', 'Face2', 'Budda', 'Dead1', 'Dead2', 'Gate',
    'Edge H', 'Edge V', 'eGrass H', 'eGrass V', 'eGround H', 'eGround V',
];

const OTHER_ITEMS = ['Player Spawn', 'Level Finish', 'Skeleton', 'Goblin', 'Boss'];

class MapEditor {
    canvas: HTMLCanvasElement;
    ctx: CanvasRenderingContext2D;
    mapWidth = 100;
    mapHeight = 16;
    tiles: number[] = [];
    mobs: MobSpawn[] = [];
    playerX = 36;
    playerY = 36;
    finishX = -100;
    finishY = -100;
    finishW = 72;
    finishH = 72;
    activeTile = 1;
    activeTab: 'tiles' | 'other' = 'tiles';
    otherIndex = -1;
    painting = false;
    erasing = false;
    mouseGX = -1;
    mouseGY = -1;

    constructor() {
        this.canvas = document.getElementById('editor-canvas') as HTMLCanvasElement;
        this.ctx = this.canvas.getContext('2d')!;
        this.ctx.imageSmoothingEnabled = false;

        this.buildTileList();
        this.buildOtherList();
        this.initEvents();
        this.newMapInternal(100, 16);
    }

    private buildTileList(): void {
        const ul = document.getElementById('editor-tile-list')!;
        ul.innerHTML = '';
        TILE_NAMES.forEach((name, i) => {
            const li = document.createElement('li');
            li.textContent = name;
            if (i === 0) li.classList.add('selected');
            li.addEventListener('click', () => {
                ul.querySelectorAll('li').forEach(el => el.classList.remove('selected'));
                li.classList.add('selected');
                this.activeTile = i + 1;
            });
            ul.appendChild(li);
        });
    }

    private buildOtherList(): void {
        const ul = document.getElementById('editor-other-list')!;
        ul.innerHTML = '';
        OTHER_ITEMS.forEach((name, i) => {
            const li = document.createElement('li');
            li.textContent = name;
            li.addEventListener('click', () => {
                ul.querySelectorAll('li').forEach(el => el.classList.remove('selected'));
                li.classList.add('selected');
                this.otherIndex = i;
            });
            ul.appendChild(li);
        });
    }

    private initEvents(): void {
        const area = document.getElementById('editor-canvas-area')!;

        this.canvas.addEventListener('mousedown', (e) => {
            if (e.button === 2) {
                this.erasing = true;
                this.handleErase(e);
            } else {
                this.painting = true;
                this.handlePaint(e);
            }
        });

        this.canvas.addEventListener('mouseleave', () => {
            this.mouseGX = -1;
            this.mouseGY = -1;
            this.redraw();
        });

        this.canvas.addEventListener('mousemove', (e) => {
            const { gx, gy } = this.tileAt(e);
            this.mouseGX = gx;
            this.mouseGY = gy;
            this.updateCoords(e);
            if (this.painting) this.handlePaint(e);
            if (this.erasing) this.handleErase(e);
            else this.redraw();
        });

        window.addEventListener('mouseup', () => {
            this.painting = false;
            this.erasing = false;
        });

        this.canvas.addEventListener('contextmenu', (e) => e.preventDefault());
    }

    private tileAt(e: MouseEvent): { gx: number; gy: number } {
        const rect = this.canvas.getBoundingClientRect();
        const scaleX = this.canvas.width / rect.width;
        const scaleY = this.canvas.height / rect.height;
        const gx = Math.floor((e.clientX - rect.left) * scaleX / TILE_SIZE);
        const gy = Math.floor((e.clientY - rect.top) * scaleY / TILE_SIZE);
        return { gx, gy };
    }

    private pixelAt(e: MouseEvent): { px: number; py: number } {
        const rect = this.canvas.getBoundingClientRect();
        const scaleX = this.canvas.width / rect.width;
        const scaleY = this.canvas.height / rect.height;
        const px = Math.floor((e.clientX - rect.left) * scaleX);
        const py = Math.floor((e.clientY - rect.top) * scaleY);
        return { px, py };
    }

    private updateCoords(e: MouseEvent): void {
        const { gx, gy } = this.tileAt(e);
        document.getElementById('editor-coords')!.textContent = `${gx}, ${gy}`;
    }

    private handlePaint(e: MouseEvent): void {
        if (this.activeTab === 'other') {
            if (this.otherIndex < 0) return;
            const { px, py } = this.pixelAt(e);
            // Convert screen Y to game Y (bottom-up)
            const gameY = this.mapHeight * TILE_SIZE - py;
            if (this.otherIndex === 0) {
                // Player spawn
                this.playerX = px;
                this.playerY = gameY;
            } else if (this.otherIndex === 1) {
                // Level finish
                this.finishX = px;
                this.finishY = gameY;
            } else {
                // Mob: 0=Skeleton, 1=Goblin, 2=Boss
                this.mobs.push({ id: this.otherIndex - 2, x: px, y: gameY });
            }
            this.painting = false; // single click for entities
            this.redraw();
            return;
        }

        const { gx, gy } = this.tileAt(e);
        if (gx >= 0 && gx < this.mapWidth && gy >= 0 && gy < this.mapHeight) {
            this.tiles[gx + gy * this.mapWidth] = this.activeTile;
            this.redraw();
        }
    }

    private handleErase(e: MouseEvent): void {
        if (this.activeTab === 'other') {
            // Remove mob near click
            const { px, py } = this.pixelAt(e);
            const gameY = this.mapHeight * TILE_SIZE - py;
            for (let i = this.mobs.length - 1; i >= 0; i--) {
                const m = this.mobs[i];
                if (Math.abs(m.x - px) < 36 && Math.abs(m.y - gameY) < 36) {
                    this.mobs.splice(i, 1);
                    this.erasing = false;
                    this.redraw();
                    return;
                }
            }
            return;
        }

        const { gx, gy } = this.tileAt(e);
        if (gx >= 0 && gx < this.mapWidth && gy >= 0 && gy < this.mapHeight) {
            this.tiles[gx + gy * this.mapWidth] = 0;
            this.redraw();
        }
    }

    open(): void {
        this.redraw();
    }

    close(): void {
        document.getElementById('editor-wrapper')!.classList.remove('active');
        document.getElementById('menu')!.style.display = 'flex';
    }

    switchTab(tab: 'tiles' | 'other', btn: HTMLElement): void {
        this.activeTab = tab;
        document.querySelectorAll('.editor-tab-bar button').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        document.getElementById('editor-tiles-panel')!.classList.toggle('active', tab === 'tiles');
        document.getElementById('editor-other-panel')!.classList.toggle('active', tab === 'other');
    }

    newMap(): void {
        const w = prompt('Map Width (tiles):', '100');
        const h = prompt('Map Height (tiles):', '16');
        if (!w || !h) return;
        const wn = parseInt(w);
        const hn = parseInt(h);
        if (isNaN(wn) || isNaN(hn) || wn < 1 || hn < 1) return;
        this.newMapInternal(wn, hn);
    }

    private newMapInternal(w: number, h: number): void {
        this.mapWidth = w;
        this.mapHeight = h;
        this.tiles = new Array(w * h).fill(0);
        this.mobs = [];
        this.playerX = 36;
        this.playerY = 36;
        this.finishX = -100;
        this.finishY = -100;
        this.canvas.width = w * TILE_SIZE;
        this.canvas.height = h * TILE_SIZE;
        this.ctx.imageSmoothingEnabled = false;
        this.redraw();
    }

    redraw(): void {
        const ctx = this.ctx;
        ctx.fillStyle = '#6b76b7';
        ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

        // Draw grid
        ctx.strokeStyle = 'rgba(255,255,255,0.08)';
        ctx.lineWidth = 1;
        for (let gx = 0; gx <= this.mapWidth; gx++) {
            ctx.beginPath();
            ctx.moveTo(gx * TILE_SIZE, 0);
            ctx.lineTo(gx * TILE_SIZE, this.canvas.height);
            ctx.stroke();
        }
        for (let gy = 0; gy <= this.mapHeight; gy++) {
            ctx.beginPath();
            ctx.moveTo(0, gy * TILE_SIZE);
            ctx.lineTo(this.canvas.width, gy * TILE_SIZE);
            ctx.stroke();
        }

        // Draw tiles
        for (let gy = 0; gy < this.mapHeight; gy++) {
            for (let gx = 0; gx < this.mapWidth; gx++) {
                const tileId = this.tiles[gx + gy * this.mapWidth];
                if (tileId === 0) continue;
                const def = TILE_DEFS[tileId];
                if (!def) continue;
                const sheet = sheets['tiles'];
                if (!sheet) continue;
                ctx.drawImage(
                    sheet,
                    def.srcX, def.srcY, def.w, def.h,
                    gx * TILE_SIZE, gy * TILE_SIZE, def.w, def.h,
                );
            }
        }

        // Draw mobs as labels
        ctx.font = '11px monospace';
        const mobNames = ['Skeleton', 'Goblin', 'Boss'];
        for (const m of this.mobs) {
            const screenY = this.mapHeight * TILE_SIZE - m.y;
            ctx.fillStyle = m.id === 1 ? '#0f0' : m.id === 2 ? '#f80' : '#f44';
            ctx.fillRect(m.x - 2, screenY - 12, ctx.measureText(mobNames[m.id] || '?').width + 4, 14);
            ctx.fillStyle = '#fff';
            ctx.fillText(mobNames[m.id] || '?', m.x, screenY);
        }

        // Draw player spawn
        {
            const sy = this.mapHeight * TILE_SIZE - this.playerY;
            ctx.fillStyle = '#28f';
            ctx.fillRect(this.playerX - 2, sy - 12, 48, 14);
            ctx.fillStyle = '#fff';
            ctx.fillText('Player', this.playerX, sy);
        }

        // Draw finish area
        if (this.finishX >= 0) {
            const fy = this.mapHeight * TILE_SIZE - this.finishY - this.finishH;
            ctx.strokeStyle = '#ff0';
            ctx.lineWidth = 2;
            ctx.strokeRect(this.finishX, fy, this.finishW, this.finishH);
            ctx.fillStyle = '#ff0';
            ctx.fillText('Finish', this.finishX + 4, fy + 12);
        }

        // Draw tile preview at cursor
        if (this.activeTab === 'tiles' && this.mouseGX >= 0 && this.mouseGY >= 0 &&
            this.mouseGX < this.mapWidth && this.mouseGY < this.mapHeight) {
            const def = TILE_DEFS[this.activeTile];
            const sheet = sheets['tiles'];
            if (def && sheet) {
                ctx.globalAlpha = 0.5;
                ctx.drawImage(
                    sheet,
                    def.srcX, def.srcY, def.w, def.h,
                    this.mouseGX * TILE_SIZE, this.mouseGY * TILE_SIZE, def.w, def.h,
                );
                ctx.globalAlpha = 1;
                ctx.strokeStyle = '#fff';
                ctx.lineWidth = 1;
                ctx.strokeRect(this.mouseGX * TILE_SIZE, this.mouseGY * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    toMapData(): string {
        const tileStr = this.tiles.join(',');
        const mobStr = this.mobs.map(m => `${m.id},${m.x},${m.y}`).join(';');
        const playerStr = `${this.playerX},${this.playerY}`;
        const finishStr = `${this.finishX},${this.finishY},${this.finishW},${this.finishH}`;
        return `${this.mapWidth},${this.mapHeight}\n${tileStr}\n${mobStr}\n${playerStr}\n${finishStr}`;
    }

    saveMap(): void {
        const name = (document.getElementById('editor-name') as HTMLInputElement).value || 'untitled';
        const data = this.toMapData();
        const blob = new Blob([data], { type: 'text/plain' });
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = name + '.txt';
        a.click();
        URL.revokeObjectURL(a.href);
    }

    loadMapPrompt(): void {
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = '.txt';
        input.addEventListener('change', () => {
            const file = input.files?.[0];
            if (!file) return;
            const reader = new FileReader();
            reader.onload = () => {
                this.loadMapData(reader.result as string);
                (document.getElementById('editor-name') as HTMLInputElement).value =
                    file.name.replace(/\.txt$/, '');
            };
            reader.readAsText(file);
        });
        input.click();
    }

    loadMapData(data: string): void {
        const lines = data.trim().split('\n');
        const [w, h] = lines[0].split(',').map(Number);
        this.mapWidth = w;
        this.mapHeight = h;
        this.tiles = lines[1].split(',').map(Number);

        this.mobs = [];
        if (lines[2] && lines[2].length > 0) {
            const parts = lines[2].split(';');
            for (const part of parts) {
                const vals = part.split(',').map(Number);
                if (vals.length >= 3) {
                    this.mobs.push({ id: vals[0], x: vals[1], y: vals[2] });
                }
            }
        }

        if (lines[3]) {
            const pvals = lines[3].split(',').map(Number);
            if (pvals.length >= 2) {
                this.playerX = pvals[0];
                this.playerY = pvals[1];
            }
        }

        if (lines[4]) {
            const fvals = lines[4].split(',').map(Number);
            if (fvals.length >= 4) {
                this.finishX = fvals[0];
                this.finishY = fvals[1];
                this.finishW = fvals[2];
                this.finishH = fvals[3];
            }
        }

        this.canvas.width = w * TILE_SIZE;
        this.canvas.height = h * TILE_SIZE;
        this.ctx.imageSmoothingEnabled = false;
        this.redraw();
    }

    playTest(): void {
        const data = this.toMapData();
        document.getElementById('editor-wrapper')!.classList.remove('active');
        game.startLevelFromData(data);
    }
}

// ===== Init =====

let game: Game;
let mapEditor: MapEditor;

window.addEventListener('load', async () => {
    try {
        await loadAssets();
        document.getElementById('loading')!.style.display = 'none';
        game = new Game();
        // Expose to window for menu button onclick handlers
        (window as any).game = game;
        const vi = document.getElementById('version-info');
        if (vi) vi.textContent = 'v' + ((window as any).GAME_VERSION || 'dev');
    } catch (e) {
        const loading = document.getElementById('loading')!;
        loading.textContent = 'Failed to load assets. Make sure to run from a local server (e.g. npx serve .)';
        console.error(e);
    }
});
