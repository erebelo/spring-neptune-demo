document.getElementById("action").addEventListener("change", function () {
  resetInputFields();
  resetGraph();

  const action = this.value;
  const inputFields = document.querySelectorAll(".input-field");

  inputFields.forEach((field) => (field.style.display = "none"));

  const sendButton = document.getElementById("sendButton");
  sendButton.style.display = "none";

  if (action === "get-all-users") {
    sendButton.style.display = "block";
  } else if (action === "get-all-users-by-params") {
    document.getElementById("username-container").style.display = "block";
    document.getElementById("address-state-container").style.display = "block";
    document.getElementById("limit-container").style.display = "block";
    document.getElementById("page-container").style.display = "block";
    sendButton.style.display = "block";
  } else if (action === "get-user-by-id") {
    document.getElementById("user-id-container").style.display = "block";
    sendButton.style.display = "block";
  }
});

document
  .getElementById("sendButton")
  .addEventListener("click", async function () {
    if (!validateForm()) {
      return;
    }

    resetGraph();

    const action = document.getElementById("action").value;
    let data;

    if (action === "get-all-users") {
      data = await fetchAllUsers();
    } else if (action === "get-all-users-by-params") {
      const username = document.getElementById("username").value;
      const addressState = document.getElementById("address-state").value;
      const limit = document.getElementById("limit").value;
      const page = document.getElementById("page").value;
      data = await fetchAllUsersByParams(username, addressState, limit, page);
    } else if (action === "get-user-by-id") {
      const userId = document.getElementById("user-id").value;
      data = await fetchUserById(userId);
    }

    initializeGraph(data);
  });

function validateForm() {
  let isValid = true;
  const userIdContainer = document.getElementById("user-id-container");

  if (
    userIdContainer.style.display !== "none" &&
    !document.getElementById("user-id").value
  ) {
    document.getElementById("user-id-error").style.display = "block";
    isValid = false;
  } else {
    document.getElementById("user-id-error").style.display = "none";
  }

  return isValid;
}

function resetInputFields() {
  document.getElementById("user-id").value = "";
  document.getElementById("username").value = "";
  document.getElementById("address-state").value = "";
  document.getElementById("limit").value = "";
  document.getElementById("page").value = "";
}

function resetGraph() {
  const cy = cytoscape({
    container: document.getElementById("cy"),
    elements: [],
    style: [],
    layout: {
      name: "cose",
      animate: true,
    },
  });

  cy.fit();
}

function layoutConfig(edgesLength) {
  // Determine layout based on the presence of edges
  const layoutConfig =
    edgesLength > 0
      ? {
          name: "cose",
          animate: true,
        }
      : {
          name: "random",
          fit: true,
        };

  return layoutConfig;
}

async function fetchAllUsers() {
  try {
    const response = await fetch(`/spring-neptune-demo/graph/data`);

    const data = await response.json();
    console.log("fetchAllUsers data", data);

    return transformGraphData(data);
  } catch (error) {
    console.error("Error hitting fetchAllUsers", error);
  }
}

async function fetchAllUsersByParams(name, addressState, limit, page) {
  try {
    const response = await fetch(
      `/spring-neptune-demo/users?name=${name}&addressState=${addressState}&limit=${limit}&page=${page}`
    );

    const data = await response.json();
    console.log("fetchAllUsersByParams data", data);

    return transformUserData(data);
  } catch (error) {
    console.error("Error hitting fetchAllUsersByParams", error);
  }
}

async function fetchUserById(userId) {
  try {
    const response = await fetch(`/spring-neptune-demo/users/${userId}`);

    const data = await response.json();
    console.log("fetchUserById data", data);

    return transformUserData(data);
  } catch (error) {
    console.error("Error hitting fetchUserById", error);
  }
}

function transformGraphData(data) {
  const output = { elements: { nodes: [], edges: [] } };

  const vertices = Object.keys(data.vertices).map((vertexId) => ({
    data: {
      id: vertexId,
      label: data.vertices[vertexId].label,
      name: data.vertices[vertexId].name,
      properties: data.vertices[vertexId],
    },
  }));

  const edges = Object.keys(data.edges).map((edgeId) => ({
    data: {
      id: edgeId,
      source: data.edges[edgeId].from,
      target: data.edges[edgeId].to,
      label: data.edges[edgeId].label,
      properties: data.edges[edgeId],
    },
  }));

  output.elements.nodes = vertices;
  output.elements.edges = edges;

  return output;
}

function transformUserData(data) {
  const output = { elements: { nodes: [], edges: [] } };
  data.forEach((item) => {
    const node = parseUserData(item);
    output.elements.nodes.push(node);
  });

  return output;
}

function transformRelationshipData(data) {
  const output = { elements: { nodes: [], edges: [] } };

  data.forEach((item) => {
    const source = parseUserData(item.entityOne);
    const target = parseUserData(item.entityTwo);

    const edge = {
      data: {
        id: item.id,
        source: item.entityOne.id,
        target: item.entityTwo.id,
        // label: item.label,
        relationshipStatus: item.relationshipStatus,
        relationshipType: item.relationshipType,
        productLob: item.productLob,
        productType: item.productType,
        startDate: item.startDate,
        endDate: item.endDate,
      },
    };

    output.elements.nodes.push(source, target);
    output.elements.edges.push(edge);
  });

  return output;
}

function parseUserData(vertex) {
  const address = vertex.address
    ? Object.entries(vertex.address).reduce((acc, [key, value]) => {
        acc[`address_${key}`] = value;
        return acc;
      }, {})
    : {};

  return {
    data: {
      id: vertex.id,
      label: "User",
      username: vertex.username,
      name: vertex.name,
      ...address,
    },
  };
}

async function initializeGraph(data) {
  const cy = cytoscape({
    container: document.getElementById("cy"),
    elements: data.elements,
    style: [
      {
        selector: "node",
        style: {
          "background-color": "#0074D9",
          label: "data(name)",
          color: "#fff",
          "text-valign": "center",
          "text-halign": "center",
          "text-wrap": "wrap",
          "text-max-width": "100px", // Adjust the max width as needed
          width: (ele) => calculateNodeSize(ele.data("name")),
          height: (ele) => calculateNodeSize(ele.data("name")),
        },
      },
      {
        selector: "edge",
        style: {
          width: 2,
          "line-color": "#0074d9",
          "target-arrow-color": "#0074d9",
          "target-arrow-shape": "triangle",
          // label: "data(label)",
          color: "#007409",
          "text-rotation": "autorotate",
        },
      },
    ],
    layout: layoutConfig(data.elements.edges.length),
  });

  cy.fit();
  addHoverFeature(cy);
}

function addHoverFeature(cy) {
  const tooltip = document.getElementById("tooltip");

  cy.on("mouseover", "node, edge", (event) => {
    const target = event.target;
    const position = target.isNode()
      ? target.renderedPosition()
      : target.renderedMidpoint();
    const data = target.data();

    tooltip.style.left = `${position.x + 270}px`;
    tooltip.style.top = `${position.y + 50}px`;

    // Create HTML content for the tooltip
    tooltip.innerHTML = `<strong>${
      target.isNode() ? "User" : "FOLLOW"
    } Details:</strong><br>${formatData(data)}`;
    tooltip.style.display = "block";
  });

  cy.on("mouseout", "node, edge", () => {
    tooltip.style.display = "none";
  });
}

function formatData(data) {
  return Object.entries(data)
    .map(([key, value]) => {
      if (typeof value === "object" && value !== null) {
        return `<strong>${key}:</strong><br>${formatData(value)}`;
      } else {
        return `<strong>${key}:</strong> ${value}`;
      }
    })
    .join("<br>");
}

function calculateNodeSize(label) {
  const baseSize = 30; // Base size for nodes
  const sizeIncrement = 5; // Increment size per character
  const maxSize = 200; // Maximum size for nodes
  const calculatedSize = baseSize + label.length * sizeIncrement;

  return Math.min(calculatedSize, maxSize);
}
