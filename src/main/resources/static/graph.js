document.getElementById("action").addEventListener("change", function () {
  resetInputFields();
  resetGraph();

  const action = this.value;

  const inputFields = document.querySelectorAll(".input-field");
  inputFields.forEach((field) => (field.style.display = "none"));

  const sendButton = document.getElementById("sendButton");

  if (action === "get-all-users-by-params") {
    document.getElementById("username-container").style.display = "block";
    document.getElementById("address-state-container").style.display = "block";
    document.getElementById("limit-container").style.display = "block";
    document.getElementById("page-container").style.display = "block";
    sendButton.disabled = false;
  } else if (action === "get-all-users-and-relationships") {
    sendButton.disabled = false;
  } else if (action === "get-user-and-relationships-by-id") {
    document.getElementById("user-id-container").style.display = "block";
    sendButton.disabled = false;
  } else {
    sendButton.disabled = true;
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

    if (action === "get-all-users-by-params") {
      const username = document.getElementById("username").value;
      const addressState = document.getElementById("address-state").value;
      const limit = document.getElementById("limit").value;
      const page = document.getElementById("page").value;
      data = await fetchAllUsersByParams(username, addressState, limit, page);
    } else if (action === "get-all-users-and-relationships") {
      data = await fetchAllUsersAndRelationships();
    } else if (action === "get-user-and-relationships-by-id") {
      const userId = document.getElementById("user-id").value;
      data = await fetchUserAndRelationshipsById(userId);
    }

    initializeGraph(data);
  });

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

function layoutConfig(edgesLength) {
  // Determine layout based on the presence of edges
  return edgesLength > 0
    ? {
        name: "cose",
        padding: 20,
        animate: true,
        fit: true,
        boundingBox: { x1: 0, y1: 0, x2: 600, y2: 400 },
      }
    : { name: "random", fit: true };
}

async function fetchAllUsersByParams(name, addressState, limit, page) {
  try {
    const response = await fetch(
      `/spring-neptune-demo/users?name=${name}&addressState=${addressState}&limit=${limit}&page=${page}`
    );

    const data = await response.json();
    console.log("fetchAllUsersByParams data", data);

    return transformUserDataList(data);
  } catch (error) {
    console.error("Error hitting fetchAllUsersByParams", error);
  }
}

async function fetchAllUsersAndRelationships() {
  try {
    const response = await fetch(`/spring-neptune-demo/graph/data`);

    const data = await response.json();
    console.log("fetchAllUsersAndRelationships data", data);

    return transformGraphData(data);
  } catch (error) {
    console.error("Error hitting fetchAllUsersAndRelationships", error);
  }
}

async function fetchUserAndRelationshipsById(userId) {
  try {
    const response = await fetch(`/spring-neptune-demo/users/${userId}`);

    const data = await response.json();
    console.log("fetchUserAndRelationshipsById data", data);

    return transformUserData(data);
  } catch (error) {
    console.error("Error hitting fetchUserAndRelationshipsById", error);
  }
}

function transformUserDataList(data) {
  const output = { elements: { nodes: [], edges: [] } };

  if (data && Array.isArray(data) && data.length > 0) {
    data.forEach((item) => {
      const node = parseUserData(item);
      output.elements.nodes.push(node);
    });
  }

  return output;
}

function transformUserData(data) {
  const output = { elements: { nodes: [], edges: [] } };

  if (data && (!data.hasOwnProperty("status") || data.status !== "NOT_FOUND")) {
    let verticesMap = new Map(),
      edgesMap = new Map();

    const checkAndAddVertex = (data) => {
      let key = data.id;
      if (!verticesMap.has(key)) {
        const vertex = parseUserData(data);
        verticesMap.set(key, vertex);
      }
    };

    const checkAndAddEdge = (data, direction, mainUserId) => {
      let key = data.id;
      if (!edgesMap.has(key)) {
        const edge = parseFollowData(data, direction, mainUserId);
        edgesMap.set(key, edge);
      }
    };

    // Add the main user vertex
    checkAndAddVertex(data);

    // Process followers
    if (
      data.followers &&
      Array.isArray(data.followers) &&
      data.followers.length > 0
    ) {
      data.followers.forEach((item) => {
        checkAndAddVertex(item.user);
        checkAndAddEdge(item, "FOLLOWERS", data.id);
      });
    }

    // Process following
    if (
      data.following &&
      Array.isArray(data.following) &&
      data.following.length > 0
    ) {
      data.following.forEach((item) => {
        checkAndAddVertex(item.user);
        checkAndAddEdge(item, "FOLLOWING", data.id);
      });
    }

    output.elements.nodes = Array.from(verticesMap.values());
    output.elements.edges = Array.from(edgesMap.values());
  }

  return output;
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

  output.elements.nodes.push(...vertices);
  output.elements.edges.push(...edges);

  return output;
}

function parseUserData(data) {
  const address = data.address
    ? Object.entries(data.address).reduce((acc, [key, value]) => {
        acc[`address_${key}`] = value;
        return acc;
      }, {})
    : {};

  return {
    data: {
      id: data.id,
      label: "User",
      name: data.name,
      properties: {
        id: data.id,
        label: "User",
        env: "dev",
        username: data.username,
        name: data.name,
        ...address,
      },
    },
  };
}

function parseFollowData(data, direction, mainUserId) {
  const from = direction === "FOLLOWING" ? mainUserId : data.user.id;
  const to = direction === "FOLLOWING" ? data.user.id : mainUserId;

  return {
    data: {
      id: data.id,
      label: "FOLLOW",
      source: from,
      target: to,
      properties: {
        id: data.id,
        label: "FOLLOW",
        env: "dev",
        status: data.status,
        startPeriod: data.startPeriod,
        endPeriod: data.endPeriod,
        from: from,
        to: to,
      },
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
          label: "data(name)",
          color: "#fff",
          "background-color": "#0074D9",
          "text-valign": "center",
          "text-halign": "center",
          "text-wrap": "wrap",
          "text-max-width": "100px",
          width: (ele) => calculateNodeSize(ele.data("name")),
          height: (ele) => calculateNodeSize(ele.data("name")),
        },
      },
      {
        selector: "edge",
        style: {
          label: "data(label)",
          color: "#000000",
          width: 3,
          "curve-style": "bezier",
          "target-arrow-shape": "triangle",
          "target-arrow-color": "#0074d9",
          "line-color": "#0074d9",
          "text-rotation": "autorotate",
          "text-background-color": "#fff",
          "text-background-opacity": 1,
          "text-background-padding": "3px",
        },
      },
    ],
    layout: layoutConfig(data.elements.edges.length),
  });

  addOpenPopupFeature(cy);
  cy.fit();
}

function calculateNodeSize(label) {
  const baseSize = 30; // Base size for nodes
  const sizeIncrement = 5; // Increment size per character
  const maxSize = 200; // Maximum size for nodes
  const calculatedSize = baseSize + label.length * sizeIncrement;

  return Math.min(calculatedSize, maxSize);
}

function addOpenPopupFeature(cy) {
  const popup = document.getElementById("popup");
  const popupContent = document.querySelector(".popup-content");
  const graphContainer = document.getElementById("graph-container");

  cy.on("tap", "node, edge", function (event) {
    const element = event.target;
    const elementType = element.isNode() ? "Vertex" : "Edge";
    const properties = element.data("properties");

    // Create HTML content for the popup
    let content = `<div class='property-label'>${element.data(
      "label"
    )} ${elementType}:</div>`;
    for (const key in properties) {
      if (properties.hasOwnProperty(key)) {
        content += `<div class='property-item'><strong>${key}:</strong> ${properties[key]}</div>`;
      }
    }

    // Set content and position of the popup
    popupContent.innerHTML = content;
    popup.style.display = "block";
    popup.style.left = `${event.renderedPosition.x}px`;
    popup.style.top = `${event.renderedPosition.y}px`;
  });

  // Close popup when tapping on cy background
  cy.on("tap", function (event) {
    if (event.target === cy) {
      closePopup();
    }
  });

  // Close popup when tapping on graph container
  document.addEventListener("click", function (event) {
    if (
      !popup.contains(event.target) &&
      !graphContainer.contains(event.target)
    ) {
      closePopup();
    }
  });
}

function closePopup() {
  popup.style.display = "none";
}
