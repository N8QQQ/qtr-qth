/**
 * qtr-qth Documentation Flare: Telemetry Pulse
 * A minimalist generative background mimicking GPS synchronization pulses.
 */

let pulses = [];

function setup() {
  const canvas = createCanvas(windowWidth, windowHeight);
  canvas.parent('p5-flare');
  noFill();
  strokeWeight(1);
}

function draw() {
  clear();
  
  // Randomly trigger a pulse (representing a GPS sentence arrival)
  if (frameCount % 60 === 0 && random() > 0.5) {
    pulses.push({
      x: random(width),
      y: random(height),
      r: 0,
      alpha: 150
    });
  }

  // Update and draw pulses
  for (let i = pulses.length - 1; i >= 0; i--) {
    let p = pulses[i];
    stroke(0, 150, 255, p.alpha); // Tech blue
    circle(p.x, p.y, p.r);
    
    p.r += 2;
    p.alpha -= 2;

    if (p.alpha <= 0) {
      pulses.splice(i, 1);
    }
  }
}

function windowResized() {
  resizeCanvas(windowWidth, windowHeight);
}
