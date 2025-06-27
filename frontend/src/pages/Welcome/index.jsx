import React, { useState } from "react";
import { 
    Typography, Box, Paper
} from "@mui/material";

import PanelRenderer from './PanelRenderer';

const Welcome = () => {
    // Estado para armazenar a estrutura de painéis em formato de árvore
    const [panelTree, setPanelTree] = useState({
        id: 1,
        children: null,
        content: null
    });

    let nextId = 2;

    // Função para dividir um painel
    const handleSplit = (panelId) => {
        // Função recursiva para encontrar e dividir o painel correto
        const splitNodeById = (node) => {
            if (node.id === panelId) {
                // Dividir este nó criando dois filhos
                return {
                    ...node,
                    children: [
                        { id: nextId++, children: null, content: null },
                        { id: nextId++, children: null, content: null }
                    ]
                };
            }
            
            // Se este nó não tem filhos, retorná-lo sem modificações
            if (!node.children) {
                return node;
            }
            
            // Procurar nos filhos e atualizá-los se necessário
            return {
                ...node,
                children: node.children.map(child => splitNodeById(child))
            };
        };

        setPanelTree(splitNodeById(panelTree));
    };

    // Função para fechar um painel dividido
    const handleClose = (panelId) => {
        // Função recursiva para encontrar e fechar o painel correto
        const closeNodeById = (node) => {
            // Se este é o nó que queremos fechar
            if (node.id === panelId) {
                // Remover os filhos, voltando ao estado original
                return {
                    ...node,
                    children: null
                };
            }
            
            // Se este nó não tem filhos, retorná-lo sem modificações
            if (!node.children) {
                return node;
            }
            
            // Procurar nos filhos e atualizá-los se necessário
            return {
                ...node,
                children: node.children.map(child => closeNodeById(child))
            };
        };

        setPanelTree(closeNodeById(panelTree));
    };
    
    // Função para adicionar conteúdo a um painel
    const handleAddContent = (panelId, content) => {
        // Função recursiva para encontrar o painel correto e adicionar conteúdo
        const addContentToNode = (node) => {
            if (node.id === panelId) {
                // Adicionar conteúdo a este nó
                return {
                    ...node,
                    content
                };
            }
            
            // Se este nó não tem filhos, retorná-lo sem modificações
            if (!node.children) {
                return node;
            }
            
            // Procurar nos filhos e atualizá-los se necessário
            return {
                ...node,
                children: node.children.map(child => addContentToNode(child))
            };
        };
        
        setPanelTree(addContentToNode(panelTree));
    };

    return (
        <Box sx={{ padding: 3, height: 'calc(100vh - 120px)' }}>
            <Typography variant="h5" gutterBottom>
                Bem-vindo ao DynamDocumentation!
            </Typography>
            <Typography variant="body1" paragraph>
                Aqui você encontra uma vasta coleção de bibliotecas e suas respectivas documentações, principalmente para Python.
                Você pode dividir os painéis clicando no ícone + e fechar divisões com o ícone X.
            </Typography>
            
            <Paper 
                elevation={3} 
                sx={{ 
                    height: 'calc(100% - 100px)', 
                    width: '100%', 
                    overflow: 'hidden' 
                }}
            >
                <PanelRenderer 
                    node={panelTree} 
                    onSplit={handleSplit} 
                    onClose={handleClose}
                    onAddContent={handleAddContent}
                />
            </Paper>
        </Box>
    );
};

export default Welcome;