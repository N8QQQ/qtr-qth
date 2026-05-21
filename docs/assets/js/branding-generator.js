/**
 * qtr-qth Branding Generator: Constellation Engine
 * Generates 1280x640 high-fidelity branding assets.
 */

let elements = [];
let themeColor;

function setup() {
  const canvas = createCanvas(1280, 640);
  canvas.parent('p5-og-branding');
  noFill();
  
  // Detect theme (simulated or from meta)
  themeColor = color(0, 150, 255); // Default Heritage Blue

  // Initialize Constellation Elements
  for (let i = 0; i < 30; i++) {
    elements.push({
      pos: createVector(random(width), random(height)),
      vel: createVector(random(-0.2, 0.2), random(-0.2, 0.2)),
      size: random(3, 8)
    });
  }
}

function draw() {
  background(10, 10, 15); // Deep Space
  drawConstellation();
  drawBranding();
}

function drawConstellation() {
  const c = themeColor;
  for (let i = 0; i < elements.length; i++) {
    let e = elements[i];
    e.pos.add(e.vel);
    
    // Bounds check
    if (e.pos.x < 0 || e.pos.x > width) e.vel.x *= -1;
    if (e.pos.y < 0 || e.pos.y > height) e.vel.y *= -1;
    
    fill(red(c), green(c), blue(c), 180);
    noStroke();
    circle(e.pos.x, e.pos.y, e.size);
    
    // Draw lines to nearby nodes
    stroke(red(c), green(c), blue(c), 60);
    for (let j = i + 1; j < elements.length; j++) {
      let d = dist(e.pos.x, e.pos.y, elements[j].pos.x, elements[j].pos.y);
      if (d < 250) {
        strokeWeight(map(d, 0, 250, 1.5, 0.1));
        line(e.pos.x, e.pos.y, elements[j].pos.x, elements[j].pos.y);
      }
    }
  }
}

function drawBranding() {
  // Title
  fill(255);
  noStroke();
  textAlign(LEFT, CENTER);
  textFont('Courier New');
  textSize(80);
  text('qtr-qth', 80, height / 2 - 40);
  
  // Subtitle
  textSize(24);
  fill(themeColor);
  text('High-Fidelity Telemetry Monad', 84, height / 2 + 30);
  
  // Tagline
  fill(150);
  textSize(18);
  text('v0.5.0 | Phase 7: The Virtual Shack', 84, height / 2 + 70);
  
  // Tech Deco
  stroke(255, 50);
  strokeWeight(1);
  line(80, height / 2 + 10, 600, height / 2 + 10);
}
