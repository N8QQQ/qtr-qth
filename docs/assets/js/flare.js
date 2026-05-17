/**
 * qtr-qth Documentation Flare
 * Randomized aesthetic modes: Pulse (Signal Lock) or Constellation (Satellite Network).
 */

let mode;
let elements = [];

function setup() {
  const canvas = createCanvas(windowWidth, windowHeight);
  canvas.parent('p5-flare');
  noFill();
  
  // Randomize mode on reload
  mode = random(['pulse', 'constellation']);
  
  if (mode === 'constellation') {
    for (let i = 0; i < 15; i++) {
      elements.push({
        pos: createVector(random(width), random(height)),
        vel: createVector(random(-0.5, 0.5), random(-0.5, 0.5)),
        size: random(2, 5)
      });
    }
  }
}

function draw() {
  clear();
  
  if (mode === 'pulse') {
    drawPulse();
  } else {
    drawConstellation();
  }
}

function drawPulse() {
  if (frameCount % 60 === 0 && random() > 0.5) {
    elements.push({
      x: random(width),
      y: random(height),
      r: 0,
      alpha: 150
    });
  }

  for (let i = elements.length - 1; i >= 0; i--) {
    let p = elements[i];
    stroke(0, 150, 255, p.alpha);
    circle(p.x, p.y, p.r);
    p.r += 2;
    p.alpha -= 2;
    if (p.alpha <= 0) elements.splice(i, 1);
  }
}

function drawConstellation() {
  stroke(0, 150, 255, 80);
  for (let i = 0; i < elements.length; i++) {
    let e = elements[i];
    e.pos.add(e.vel);
    
    // Bounds check
    if (e.pos.x < 0 || e.pos.x > width) e.vel.x *= -1;
    if (e.pos.y < 0 || e.pos.y > height) e.vel.y *= -1;
    
    fill(0, 150, 255, 100);
    noStroke();
    circle(e.pos.x, e.pos.y, e.size);
    
    // Draw lines to nearby nodes
    stroke(0, 150, 255, 30);
    for (let j = i + 1; j < elements.length; j++) {
      let d = dist(e.pos.x, e.pos.y, elements[j].pos.x, elements[j].pos.y);
      if (d < 200) line(e.pos.x, e.pos.y, elements[j].pos.x, elements[j].pos.y);
    }
  }
}

function windowResized() {
  resizeCanvas(windowWidth, windowHeight);
}
