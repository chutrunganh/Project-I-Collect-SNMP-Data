FROM openjdk:21-jdk-slim-buster

# Install necessary libraries for JavaFX, GTK, Xvfb, and VNC
RUN apt-get update && apt-get install -y \
    # X11 libraries for graphical applications
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    # OpenGL libraries
    libgl1-mesa-glx \
    # GTK and its dependencies for GUI applications
    libgtk-3-0 \
    libgdk-pixbuf2.0-0 \
    libglib2.0-0 \
    libcairo2 \
    libpango-1.0-0 \
    libatk1.0-0 \
    # Xvfb for virtual framebuffer X server
    xvfb \
    # x11vnc for VNC server
    x11vnc \
    # Clean up the apt cache to reduce image size
    && rm -rf /var/lib/apt/lists/*

# Copy the packaged jar file and MIB Databases into the container
# So make sure you run this Dockerfile in the same directory as the jar file and MIB Databases
COPY . /SNMP_Browser/

# Set the working directory in the container
WORKDIR /SNMP_Browser

# Export for VNC server.  
# This is used by the VNC server but doesn't actually publish the port
ENV VNC_PORT=5900
# Why need the to use a VNC server?
# The JavaFX application requires a graphical display to run, but Docker containers don't have a display by default.
# So, we need to use a virtual display provided by Xvfb and a VNC server to access the display remotely so
# that we can see the GUI of the JavaFX application.

# Expose the display port for X11
# This is used by Xvfb to create a virtual display
ENV DISPLAY=:99

# Start Xvfb and VNC server, then run the Java application
CMD ["sh", "-c", "Xvfb :99 -ac & x11vnc -display :99 -forever -nopw -rfbport 5900 & java -jar 'SNMP Browser.jar'"]
# What does CMD do:
# 1. Start Xvfb on display :99 with the -ac option to disable access control
# 2. Start x11vnc on display :99 with the -forever option to keep the server running
# and the -nopw option to disable password authentication, and the -rfbport option to set the VNC port
# 3. Run the Java application with the -jar option to specify the jar file to run


# !!! STILL NOT WORKING !!!
# The image builds successfully, and I can connect to the VNC Viewer (TigerVNC Viewer) on localhost:5900 
# (using port binding 5900 to host) to see the application's GUI. However, the application cannot connect 
# to the outside network to perform SNMP requests. Tried running the container with the --network="host" 
# option, but it still doesn't work.