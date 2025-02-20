function calculateSquareCorners(startX, startY, startZ, size = 29, type) {
  const endX = startX + (size - 1); // Tiến sang phải trên trục X
  const endZ = startZ + (size - 1); // Tiến lên trên trục Z (hướng South)

  const corners = {
    bottomLeft: [startX, startY, startZ], // Góc dưới bên trái (gốc đầu vào)
    bottomRight: [endX, startY, startZ], // Góc dưới bên phải
    topLeft: [startX, startY, endZ], // Góc trên bên trái
    topRight: [endX, startY, endZ], // Góc trên bên phải
  };

  const javaCorners = `private final List<Vec3d> ${type}Locations = Arrays.asList(
      new Vec3d(${startX}, ${startY}, ${startZ}),
      new Vec3d(${startX}, ${startY}, ${endZ}),
      new Vec3d(${endX}, ${startY}, ${endZ}),
      new Vec3d(${endX}, ${startY}, ${startZ})
    );`;

  return javaCorners;
}

// 701.653 143.06250 -1156.1156 -- gold
// 701.204 143.00000 -1228.482 -- iron
// 701.338 143.50000 -796.487 -- copper
//  701.509 143.5000 -1300.346 -- coal
// 730.538 143.0000 -293.451 -- stone

// 699 143 -1014/058 redstone

const lapisLocations = [703, 143, -1083];
const restoneLocations = [704, 143, -1011];
const copperLocations = [704, 143, -795];
const coalLocations = [703, 143, -1299];
const emeraldLocations = [704, 142, -940];

const lapis = calculateSquareCorners(
  lapisLocations[0],
  lapisLocations[1],
  lapisLocations[2],
  25,
  'lapis',
);

const redstone = calculateSquareCorners(
  restoneLocations[0],
  restoneLocations[1],
  restoneLocations[2],
  25,
  'redstone',
);

const copper = calculateSquareCorners(
  copperLocations[0],
  copperLocations[1],
  copperLocations[2],
  25,
  'copper',
);

const coal = calculateSquareCorners(
  coalLocations[0],
  coalLocations[1],
  coalLocations[2],
  25,
  'coal',
);

const emerald = calculateSquareCorners(
  emeraldLocations[0],
  emeraldLocations[1],
  emeraldLocations[2],
  25,
  'emerald',
);

console.log(emerald);

console.log(lapis);
// console.log(redstone);
// console.log(copper);
// console.log(coal);
