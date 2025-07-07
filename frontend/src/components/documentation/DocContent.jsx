import React, { useEffect, useState, useCallback } from "react";
import { Box, Typography, Paper, CircularProgress, Alert } from "@mui/material";
import axios from 'axios';

// Component to display documentation content
const DocContent = ({ content }) => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [entityDetails, setEntityDetails] = useState(null);
    
    // Define fetchEntityDetails first with useCallback
    const fetchEntityDetails = useCallback(async (entityId) => {
        setLoading(true);
        setError(null);
        try {
            // Use the type field to determine the correct endpoint
            let url;
            if (content.type === "class") {
                url = `/class/${entityId}`;
            } else if (content.type === "function") {
                url = `/function/${entityId}`;
            } else {
                throw new Error("Unknown entity type");
            }
            const response = await axios.get(url);
            console.log("API Response for entity details:", response.data);
            if (response.data && response.data.entity) {
                setEntityDetails(response.data);
            } else {
                console.warn("Unexpected data format received from server");
                setError("Failed to load entity details. Unexpected data format.");
            }
        } catch (error) {
            console.error("Error fetching entity details:", error);
            setError(`Failed to load details for ${content?.name || "entity"}.`);
        } finally {
            setLoading(false);
        }
    }, [content?.name, content?.type]); // Add content.type to dependencies
    
    // Then use it in useEffect
    useEffect(() => {
        // If content has an id, fetch the entity details from the API
        if (content && content.id) {
            console.log("Content passed to DocContent:", content);
            fetchEntityDetails(content.id);
        }
    }, [content, fetchEntityDetails]);
    
    // For debugging - log what's being used to display
    useEffect(() => {
        if (entityDetails) {
            console.log("Using entity details from API:", entityDetails);
        } else if (content) {
            console.log("Using content directly:", content);
        }
    }, [entityDetails, content]);
    
    if (!content) return null;
    
    // Note: We're using entityDetails or content directly in each function below,
    // so we don't need a separate displayData variable
    
    // Format parameters from the API response
    const formatParameters = () => {
        console.log("Formatting parameters with entityDetails:", entityDetails);
        console.log("Formatting parameters with content:", content);
        
        if (entityDetails && entityDetails.parameters && entityDetails.parameters.length > 0) {
            console.log("Using API parameters:", entityDetails.parameters);
            return entityDetails.parameters.map(param => (
                <Box key={param.id || param.name} sx={{ mb: 1 }}>
                    <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                        <code>{param.name}</code> {param.dataType ? `(${param.dataType})` : ''}
                    </Typography>
                    <Typography variant="body2" sx={{ ml: 2 }}>
                        {param.description || param.defaultValue || ''}
                    </Typography>
                </Box>
            ));
        } else if (content.docstring?.parameters && typeof content.docstring.parameters === 'object') {
            console.log("Using docstring parameters:", content.docstring.parameters);
            
            // Handle parameters object (either array or map)
            if (Array.isArray(content.docstring.parameters)) {
                return content.docstring.parameters.map(param => (
                    <Box key={param.id || param.name} sx={{ mb: 1 }}>
                        <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                            <code>{param.name}</code> {param.dataType ? `(${param.dataType})` : ''}
                        </Typography>
                        <Typography variant="body2" sx={{ ml: 2 }}>
                            {param.description || param.defaultValue || ''}
                        </Typography>
                    </Box>
                ));
            } else {
                // Handle parameters as object map
                return Object.entries(content.docstring.parameters).map(([name, param]) => (
                    <Box key={name} sx={{ mb: 1 }}>
                        <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                            <code>{name}</code> {param.type ? `(${param.type})` : ''}
                        </Typography>
                        <Typography variant="body2" sx={{ ml: 2 }}>
                            {param.description}
                        </Typography>
                    </Box>
                ));
            }
        } else if (content.parameters && Array.isArray(content.parameters)) {
            console.log("Using content parameters array:", content.parameters);
            return content.parameters.map(param => (
                <Box key={param.id || param.name} sx={{ mb: 1 }}>
                    <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                        <code>{param.name}</code> {param.dataType || param.type ? `(${param.dataType || param.type})` : ''}
                    </Typography>
                    <Typography variant="body2" sx={{ ml: 2 }}>
                        {param.description || param.defaultValue || ''}
                    </Typography>
                </Box>
            ));
        }
        
        console.log("No parameters found to display");
        return null;
    };
    
    // Get the description from either API data or docstring
    const getDescription = () => {
        if (entityDetails && entityDetails.attributes && entityDetails.attributes.length > 0) {
            // Join all descriptions from attributes
            return entityDetails.attributes.map(attr => attr.value).join('\n\n');
        }
        return content.docstring?.description || content.description || '';
    };
    
    // Get the return value from either API data or docstring
    const getReturns = () => {
        if (entityDetails && entityDetails.returns && entityDetails.returns.length > 0) {
            return entityDetails.returns.map(ret => ret.value).join('\n\n');
        }
        return content.docstring?.returns || content.returns || '';
    };
    
    // Get entity signature from the appropriate source
    const getSignature = () => {
        if (entityDetails?.entity?.signature) {
            return entityDetails.entity.signature;
        } else if (content.signature) {
            return content.signature;
        }
        return '';
    };
    
    // Debug information
    console.log("Parameters section will show:", formatParameters() ? "Yes" : "No");
    console.log("Description value:", getDescription());
    console.log("Returns value:", getReturns());
    
    return (
        <Box sx={{ p: 1 }}>
            {loading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
                    <CircularProgress />
                </Box>
            ) : (
                <>
                    {error && (
                        <Alert severity="warning" sx={{ mb: 2 }}>
                            {error}
                        </Alert>
                    )}
                
                    <Typography variant="h6" gutterBottom>
                        {entityDetails?.entity?.name || content.name}
                        {getSignature() && (
                            <Typography variant="body2" component="span" sx={{ display: 'block', fontFamily: 'monospace', mt: 1 }}>
                                {getSignature()}
                            </Typography>
                        )}
                    </Typography>
                    
                    {/* Description section */}
                    {getDescription() && (
                        <Box sx={{ mb: 2 }}>
                            <Typography variant="subtitle1">Descrição:</Typography>
                            <Paper variant="outlined" sx={{ p: 1, mb: 1, backgroundColor: 'rgba(0,0,0,0.02)' }}>
                                <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                                    {getDescription()}
                                </Typography>
                            </Paper>
                        </Box>
                    )}
                    
                    {/* Parameters section - Only show if parameters exist */}
                    {(entityDetails?.parameters?.length > 0 || 
                      (content.docstring?.parameters && 
                        ((Array.isArray(content.docstring.parameters) && content.docstring.parameters.length > 0) ||
                        Object.keys(content.docstring.parameters || {}).length > 0)) ||
                      (content.parameters && Array.isArray(content.parameters) && content.parameters.length > 0)) && (
                        <Box sx={{ mb: 2 }}>
                            <Typography variant="subtitle1">Parâmetros:</Typography>
                            {formatParameters()}
                        </Box>
                    )}
                    
                    {/* Returns section */}
                    {getReturns() && (
                        <Box sx={{ mb: 2 }}>
                            <Typography variant="subtitle1">Retorna:</Typography>
                            <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                                {getReturns()}
                            </Typography>
                        </Box>
                    )}
                    
                    {/* Examples section - check multiple sources */}
                    {(content.docstring?.examples || content.example) && (
                        <Box sx={{ mb: 2 }}>
                            <Typography variant="subtitle1">Exemplos:</Typography>
                            <Paper 
                                variant="outlined" 
                                sx={{ 
                                    p: 1, 
                                    backgroundColor: 'rgba(0,0,0,0.05)', 
                                    fontFamily: 'monospace',
                                    whiteSpace: 'pre-wrap'
                                }}
                            >
                                <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                                    {content.docstring?.examples || content.example}
                                </Typography>
                            </Paper>
                        </Box>
                    )}
                </>
            )}
        </Box>
    );
};

export default DocContent;
