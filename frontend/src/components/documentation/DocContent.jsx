import React, { useEffect, useState } from "react";
import { Box, Typography, Paper, CircularProgress, Alert } from "@mui/material";
import axios from 'axios';

// Component to display documentation content
const DocContent = ({ content }) => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [entityDetails, setEntityDetails] = useState(null);
    
    useEffect(() => {
        // If content has an id, fetch the entity details from the API
        if (content && content.id) {
            console.log("Content passed to DocContent:", content);
            fetchEntityDetails(content.id);
        }
    }, [content]);
    
    const fetchEntityDetails = async (entityId) => {
        setLoading(true);
        setError(null);
        
        try {
            // Make API call to backend to get the entity details
            const response = await axios.get(`/entity/${entityId}`);
            console.log("API Response for entity details:", response.data);
            
            if (response.data && response.data.entity) {
                setEntityDetails(response.data);
            } else {
                console.warn("Unexpected data format received from server");
                setError("Failed to load entity details. Unexpected data format.");
            }
        } catch (error) {
            console.error("Error fetching entity details:", error);
            setError(`Failed to load details for ${content.name}.`);
        } finally {
            setLoading(false);
        }
    };
    
    // For debugging - log what's being used to display
    useEffect(() => {
        if (entityDetails) {
            console.log("Using entity details from API:", entityDetails);
        } else if (content) {
            console.log("Using content directly:", content);
        }
    }, [entityDetails, content]);
    
    if (!content) return null;
    
    // If we have entity details from the API, use those
    // Otherwise, fall back to the content prop (which might have hardcoded data)
    const displayData = entityDetails || content;
    
    // Format parameters from the API response
    const formatParameters = () => {
        console.log("Formatting parameters with entityDetails:", entityDetails);
        console.log("Formatting parameters with content:", content);
        
        if (entityDetails && entityDetails.parameters && entityDetails.parameters.length > 0) {
            console.log("Using API parameters:", entityDetails.parameters);
            return entityDetails.parameters.map(param => (
                <Box key={param.id || param.name} sx={{ mb: 1 }}>
                    <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                        <code>{param.name}</code> {param.type ? `(${param.type})` : ''}
                    </Typography>
                    <Typography variant="body2" sx={{ ml: 2 }}>
                        {param.value}
                    </Typography>
                </Box>
            ));
        } else if (content.docstring?.parameters) {
            console.log("Using docstring parameters:", content.docstring.parameters);
            // Fallback to using docstring parameters
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
        } else if (content.parameters && Array.isArray(content.parameters)) {
            console.log("Using content parameters array:", content.parameters);
            return content.parameters.map(param => (
                <Box key={param.id || param.name} sx={{ mb: 1 }}>
                    <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                        <code>{param.name}</code> {param.type ? `(${param.type})` : ''}
                    </Typography>
                    <Typography variant="body2" sx={{ ml: 2 }}>
                        {param.description || param.value || ''}
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
                      Object.keys(content.docstring?.parameters || {}).length > 0) && (
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
                    
                    {/* Examples section - still using docstring data */}
                    {content.docstring?.examples && (
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
                                    {content.docstring.examples}
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
