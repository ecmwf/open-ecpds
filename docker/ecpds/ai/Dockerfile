# Base image: Rocky Linux 9                                                                                                                                           
FROM rockylinux/rockylinux:9.6                                                                                                                                        
                                                                                                                                                                      
# Metadata                                                                                                                                                            
LABEL maintainer="Laurent.Gougeon@ecmwf.int"                                                                                                                          
LABEL org.opencontainers.image.source="https://github.com/ecmwf/open-ecpds"                                                                                           
LABEL org.opencontainers.image.description="OpenECPDS Ollama LLM Server with pre-bundled models"                                                                      
                                                                                                                                                                      
# Build arguments                                                                                                                                                     
ARG OLLAMA_FAST_MODEL_NAME                                                                                                                                            
ARG OLLAMA_DEEP_MODEL_NAME                                                                                                                                            
ARG OLLAMA_EMBD_MODEL_NAME                                                                                                                                            
                                                                                                                                                                      
# Export as environment variables                                                                                                                                     
ENV OLLAMA_FAST_MODEL_NAME=${OLLAMA_FAST_MODEL_NAME} \                                                                                                                
    OLLAMA_DEEP_MODEL_NAME=${OLLAMA_DEEP_MODEL_NAME} \                                                                                                                
    OLLAMA_EMBD_MODEL_NAME=${OLLAMA_EMBD_MODEL_NAME} \                                                                                                                
    OLLAMA_HOST=0.0.0.0 \                                                                                                                                             
    OLLAMA_MODELS=/var/lib/ai \                                                                                                                                       
    OLLAMA_KEEP_ALIVE=-1 \                                                                                                                                            
    OLLAMA_MAX_LOADED_MODELS=3 \                                                                                                                                      
    OLLAMA_NUM_THREADS=36 \                                                                                                                                           
    OLLAMA_NUM_PARALLEL=2 \                                                                                                                                           
    OLLAMA_BATCH_SIZE=1024 \                                                                                                                                          
    OLLAMA_NO_MMAP=1 \                                                                                                                                                
    OLLAMA_LLM_LIBRARY=cpu                                                                                                                                            
                                                                                                                                                                      
# Install dependencies, Ollama, and create model directory in one layer                                                                                               
RUN dnf install -y epel-release \                                                                                                                                     
    && dnf install -y tini tar gzip zstd numactl \                                                                                                                    
    && curl -fsSL https://ollama.com/install.sh | sh \                                                                                                                
    && mkdir -p /var/lib/ai \                                                                                                                                         
    && dnf clean all                                                                                                                                                  
                                                                                                                                                                      
# Declare persistent model volume                                                                                                                                     
VOLUME /var/lib/ai                                                                                                                                                    
                                                                                                                                                                      
# Copy entrypoint                                                                                                                                                     
COPY entrypoint.sh /entrypoint.sh                                                                                                                                     
RUN chmod 0755 /entrypoint.sh                                                                                                                                         
                                                                                                                                                                      
# Expose Ollama HTTP API port                                                                                                                                         
EXPOSE 11434                                                                                                                                                          
                                                                                                                                                                      
# Healthcheck                                                                                                                                                         
HEALTHCHECK --interval=30s --timeout=10s --start-period=20s \                                                                                                         
  CMD curl -f http://localhost:11434/ || exit 1                                                                                                                       
                                                                                                                                                                      
# Run entrypoint under Tini with subreaper support                                                                                                                    
ENTRYPOINT ["/usr/bin/tini","-s","--","/entrypoint.sh"]                                                                                                               
                                                                                                                                                                      