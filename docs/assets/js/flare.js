/**
 * qtr-qth Documentation Flare: Constellation Mode
 * A generative satellite network representing the GPS constellation.
 * Theme-aware color palettes.
 */

let elements = [];
let themeColor;

function setup() {
  const canvas = createCanvas(windowWidth, windowHeight);
  canvas.parent('p5-flare');
  noFill();
  
  // Detect theme and set color palette
  const theme = document.querySelector('meta[name="jekyll-theme"]')?.content || 'dinky';
  setPalette(theme);

  // Initialize Constellation Elements (Satellites)
  for (let i = 0; i < 20; i++) {
    elements.push({
      pos: createVector(random(width), random(height)),
      vel: createVector(random(-0.3, 0.3), random(-0.3, 0.3)),
      size: random(2, 6)
    });
  }
}

function setPalette(theme) {
  switch(theme) {
    case 'hacker':
      themeColor = color(0, 255, 100); // Matrix Green
      break;
    case 'midnight':
      themeColor = color(0, 200, 255); // Deep Cyan
      break;
    case 'dinky':
      themeColor = color(0, 150, 255); // Vibrant Tech Blue for Dinky
      break;
    default:
      themeColor = color(0, 150, 255); // Heritage Blue
  }
}

function draw() {
  clear();
  drawConstellation();
}

function drawConstellation() {
  const c = themeColor;
  for (let i = 0; i < elements.length; i++) {
    let e = elements[i];
    e.pos.add(e.vel);
    
    // Bounds check
    if (e.pos.x < 0 || e.pos.x > width) e.vel.x *= -1;
    if (e.pos.y < 0 || e.pos.y > height) e.vel.y *= -1;
    
    fill(red(c), green(c), blue(c), 150);
    noStroke();
    circle(e.pos.x, e.pos.y, e.size);
    
    // Draw lines to nearby nodes
    stroke(red(c), green(c), blue(c), 50);
    for (let j = i + 1; j < elements.length; j++) {
      let d = dist(e.pos.x, e.pos.y, elements[j].pos.x, elements[j].pos.y);
      if (d < 200) line(e.pos.x, e.pos.y, elements[j].pos.x, elements[j].pos.y);
    }
  }
}

function windowResized() {
  resizeCanvas(windowWidth, windowHeight);
}
